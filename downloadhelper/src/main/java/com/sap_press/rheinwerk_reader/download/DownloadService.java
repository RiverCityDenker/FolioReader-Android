package com.sap_press.rheinwerk_reader.download;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.sap_press.rheinwerk_reader.download.events.FinishDownloadContentEvent;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.download.api.ApiClient;
import com.sap_press.rheinwerk_reader.download.api.ApiService;
import com.sap_press.rheinwerk_reader.download.datamanager.DownloadDataManager;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.LibraryTable;
import com.sap_press.rheinwerk_reader.download.events.CancelDownloadEvent;
import com.sap_press.rheinwerk_reader.download.events.DestroyDownloadServiceEvent;
import com.sap_press.rheinwerk_reader.download.events.DownloadingErrorEvent;
import com.sap_press.rheinwerk_reader.download.events.DownloadingEvent;
import com.sap_press.rheinwerk_reader.download.util.DownloadUtil;
import com.sap_press.rheinwerk_reader.googleanalytics.AnalyticViewName;
import com.sap_press.rheinwerk_reader.googleanalytics.GoogleAnalyticManager;
import com.sap_press.rheinwerk_reader.mod.models.foliosupport.EpubBook;
import com.sap_press.rheinwerk_reader.utils.FileUtil;
import com.sap_press.rheinwerk_reader.utils.MemoryUtil;
import com.sap_press.rheinwerk_reader.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.sap_press.rheinwerk_reader.download.events.UnableDownloadEvent.DownloadErrorType.NOT_ENOUGH_SPACE;
import static com.sap_press.rheinwerk_reader.download.util.DownloadUtil.ReadingType.OFFLINE;
import static com.sap_press.rheinwerk_reader.download.util.DownloadUtil.ReadingType.ONLINE;
import static com.sap_press.rheinwerk_reader.mod.aping.BookApi.FILE_PATH_DEFAULT;
import static com.sap_press.rheinwerk_reader.utils.Constant.X_CONTENT_KEY;
import static com.sap_press.rheinwerk_reader.utils.Util.isOnline;

public class DownloadService extends Service {

    public static final String TAG = DownloadService.class.getSimpleName();
    public static final int DOWNLOAD_COMPLETED = 100;
    private static final int SERVICE_FOREGROUND_ID = 2018;
    private static final String NOTIFICATION_ICON = "notification_icon";
    private static final String NOTIFICATION_TITLE = "notification_title";
    private static final String BASE_URL_KEY = "base_url";
    private static final String APP_VERSION_KEY = "app_version";
    private static final String CONTENT_FILE_PATH = "content_file_path";
    private Object LOCK_SHUT_DOWN = new Object();
    GoogleAnalyticManager googleAnalyticManager;
    CompositeDisposable compositeSubscription;
    DownloadDataManager dataManager;

    private long progress = 0;
    private ThreadPoolExecutor executor;
    private int currentEbookId = 0;
    private String mCurrentBookId;
    private int mIconId;
    private String mTitle;
    private String mBaseUrl;
    private String mAppVersion;
    private String mContentFileDefault = FILE_PATH_DEFAULT;
    private ApiService mApiService;

    public DownloadService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        googleAnalyticManager = new GoogleAnalyticManager(this);
        dataManager = DownloadDataManager.getInstance();
        compositeSubscription = new CompositeDisposable();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void startDownloadService(Context context, int iconId, String title,
                                            String baseUrl, String appVersion, String contentFile) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(NOTIFICATION_ICON, iconId);
        intent.putExtra(NOTIFICATION_TITLE, title);
        intent.putExtra(BASE_URL_KEY, baseUrl);
        intent.putExtra(APP_VERSION_KEY, appVersion);
        intent.putExtra(CONTENT_FILE_PATH, contentFile);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mIconId = intent.getIntExtra(NOTIFICATION_ICON, 0);
            mTitle = intent.getStringExtra(NOTIFICATION_TITLE);
            mBaseUrl = intent.getStringExtra(BASE_URL_KEY);
            mAppVersion = intent.getStringExtra(APP_VERSION_KEY);
            mContentFileDefault = intent.getStringExtra(CONTENT_FILE_PATH);
            mApiService = ApiClient.getClient(this, mBaseUrl).create(ApiService.class);
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(mIconId)
                    .setContentTitle(mTitle)
                    .setContentText("Downloading...").build();

            startForeground(SERVICE_FOREGROUND_ID, notification);
            downloadNextOrStop();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        compositeSubscription.dispose();
        stopForeground(true);
        this.stopSelf();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCancelDownloadEvent(CancelDownloadEvent event) {
        if (event.getEbook() != null) {
            if (currentEbookId == event.getEbook().getId()) {
                currentEbookId = 0;
                Ebook ebook = event.getEbook();
                ebook.setDownloadProgress(-1);
                dataManager.updateEbook(ebook);
                if (executor != null) {
                    shutdownAndAwaitTermination(executor);
                }
                downloadNextOrStop();
            }
        } else {
            Ebook ebook = LibraryTable.getEbook(currentEbookId);
            ebook.setDownloadProgress(-1);
            dataManager.updateEbook(ebook);
            if (executor != null) {
                shutdownAndAwaitTermination(executor);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDestroyDownloadServiceEvent(DestroyDownloadServiceEvent event) {
        this.stopSelf();
        onDestroy();
    }

    private void downloadNextOrStop() {
        List<Ebook> ebookList = dataManager.getAllWaitingDownloadEbooks();
        if (!ebookList.isEmpty()) {
            final Ebook ebook = ebookList.get(0);
            final long availableSize = MemoryUtil.getAvailableInternalMemorySize();
            if (availableSize > ebook.getFileSize()) {
                downloadEbook(ebook);
            } else {
                DownloadUtil.stopDownloadServiceIfNeeded(this, NOT_ENOUGH_SPACE);
            }
        } else {
            this.stopSelf();
        }
    }

    public void downloadEbook(Ebook ebook) {
        dataManager.updateEbook(ebook);
        googleAnalyticManager.sendEvent(AnalyticViewName.start_download_start, AnalyticViewName.download_start, ebook.getTitle(), (long) ebook.getFileSize());
        dataManager.saveTimestampDownload(ebook.getTitle());
        currentEbookId = ebook.getId();
        final String token = dataManager.getAccessToken();
        downloadContent(ebook, token, mAppVersion, OFFLINE);
    }

    private void downloadContent(Ebook ebook, String token, String mAppVersion, DownloadUtil.ReadingType readingType) {
        final String ebookId = String.valueOf(ebook.getId());
        final Disposable subscription = mApiService.download(ebookId, token, mAppVersion, mAppVersion, mContentFileDefault)
                .map(responseBody -> FileUtil.writeResponseBodyToDisk(this, responseBody, ebookId, mContentFileDefault))
                .map(FileUtil::parseContentFileToObject)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(o -> downloadContentSuccess(ebook, o, token, readingType), throwable -> {
                    handleError(throwable, ebookId, null);
                });

        compositeSubscription.add(subscription);
    }

    private void handleError(Throwable throwable, String ebookId, ThreadPoolExecutor executor) {
        if (ebookId.equalsIgnoreCase(mCurrentBookId)) return;
        mCurrentBookId = ebookId;
        String url = mBaseUrl + "ebooks/" + ebookId + "/download";
        googleAnalyticManager.sendEvent(AnalyticViewName.download_error, url, DownloadUtil.getErrorCode(throwable));

        if (executor != null) {
            shutdownAndAwaitTermination(executor);
        }
        if (isOnline(this)) {
            EventBus.getDefault().post(new DownloadingErrorEvent(Integer.parseInt(ebookId)));
            downloadNextOrStop();
        }
        // Comment this line because this ticket : https://2denker.atlassian.net/browse/RE-431
        //listener.handleDownloadError(throwable);
    }

    void shutdownAndAwaitTermination(ExecutorService pool) {
        synchronized (DownloadService.class) {
            pool.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
                    pool.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!pool.awaitTermination(1, TimeUnit.SECONDS)) ;
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                pool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
    }

    private void downloadContentSuccess(Ebook ebook, Object object, String token, DownloadUtil.ReadingType readingType) {
        if (readingType.equals(ONLINE)) {
            final String filePath = FileUtil.getEbookPath(this, String.valueOf(ebook.getId()));
            ebook.setFilePath(filePath);
            dataManager.updateEbookPath(ebook.getId(), filePath);
            EventBus.getDefault().post(new FinishDownloadContentEvent(ebook));
        } else {
            long timeDownload = TimeUnit.SECONDS.toSeconds(Util.getCurrentTimeStamp() - dataManager.getTimestampDownload(ebook.getTitle()));
            googleAnalyticManager.sendEvent(AnalyticViewName.dowload_load_time, AnalyticViewName.download_duration, ebook.getTitle(), timeDownload);
            final String filePath = FileUtil.getEbookPath(this, String.valueOf(ebook.getId()));
            ebook.setFilePath(filePath);
            dataManager.updateEbookPath(ebook.getId(), filePath);
            downloadAllFiles(((EpubBook) object), token, ebook);
        }
    }

    private void downloadAllFiles(EpubBook epubBook, String token, Ebook ebook) {
        ebook.setTotal(epubBook.manifestList.size());
        final String ebookId = String.valueOf(ebook.getId());
        ArrayList<EpubBook.Manifest> fileListForDownload = getListFileForDownload(epubBook.manifestList, ebookId);
        progress = FileUtil.getFilesCount(ebook.getFilePath());
        int totalFileListNeedToDownload = fileListForDownload.size();
        executor = ParallelExecutorTask.createPool();
        if (totalFileListNeedToDownload > 0) {
            for (int i = 0; i < totalFileListNeedToDownload; i++) {
                final EpubBook.Manifest manifest = fileListForDownload.get(i);
                DownloadFileAsyn downloadFileAsyn = new DownloadFileAsyn(ebook, token, i, executor);
                downloadFileAsyn.executeParallel(manifest);
            }
        } else {
            ebook.setDownloadProgress(DOWNLOAD_COMPLETED);
            downloadSingleSucscess(ebook, ++progress);
        }
    }

    class DownloadFileAsyn extends ParallelExecutorTask<EpubBook.Manifest, Integer, Ebook> {
        Ebook ebook;
        String token;
        String ebookId;
        int name;

        DownloadFileAsyn(Ebook ebook, String token, int name, ThreadPoolExecutor executor) {
            super(executor);
            this.ebook = ebook;
            this.token = token;
            this.ebookId = String.valueOf(ebook.getId());
            this.name = name;
        }

        @Override
        protected Ebook doInBackground(EpubBook.Manifest... manifests) {
            if (isStop()) return null;
            executor = getPoolExecutor();
            EpubBook.Manifest manifest = manifests[0];

            final String fileUrl = mBaseUrl + "ebooks/" + ebookId + "/download?app_version=" + mAppVersion + "&file_path=" + manifest.getHref();
            String contentKey;
            try {
                contentKey = downloadFile(fileUrl, token, ebookId, manifest.getHref());
            } catch (IOException e) {
                e.printStackTrace();
                handleError(e, ebookId, getPoolExecutor());
                return null;
            }

            if (isStop()) return null;
            if (!TextUtils.isEmpty(contentKey) && !contentKey.equals(ebook.getContentKey())) {
                ebook.setContentKey(contentKey);
            }

            synchronized (DownloadService.class) {
                downloadSingleSucscess(ebook, ++progress);
            }

            return ebook;
        }

        private String downloadFile(String fileUrl, String token, String ebookId, String href) throws IOException {
            String contentKey = "";
            File file = FileUtil.getFile(DownloadService.this, ebookId, href);
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(60 * 1000);
            connection.setConnectTimeout(60 * 1000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", token);
            connection.setRequestProperty("x-project", "sap-press");
            connection.setRequestProperty("app_version", mAppVersion);
            connection.setRequestProperty("file_path", href);
            final int BUFFER_SIZE = 23 * 1024;
            InputStream is = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[BUFFER_SIZE];
            int current;

            while ((current = bis.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, current);
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer.toByteArray());
            fos.flush();
            fos.close();
            return connection.getHeaderField(X_CONTENT_KEY);
        }
    }

    private ArrayList<EpubBook.Manifest> getListFileForDownload(ArrayList<EpubBook.Manifest> manifestList, String ebookId) {
        deleteSomeHTMLFile(manifestList, ebookId);
        ArrayList<EpubBook.Manifest> downloadFileList = new ArrayList<>();
        for (EpubBook.Manifest manifest : manifestList) {
            if (!FileUtil.isFileExist(this, ebookId, manifest.getHref())) {
                downloadFileList.add(manifest);
            }
        }
        return downloadFileList;
    }

    private void deleteSomeHTMLFile(ArrayList<EpubBook.Manifest> manifestList, String ebookId) {
        int deleteCount = 0;
        for (EpubBook.Manifest manifest : manifestList) {
            if (manifest.getHref().contains("html") && FileUtil.isFileExist(this, ebookId, manifest.getHref()) && deleteCount < 5) {
                deleteCount++;
                FileUtil.deleteFile(FileUtil.getFile(this, ebookId, manifest.getHref()));
            }
        }
    }

    private synchronized void downloadSingleSucscess(Ebook ebook, long fileCount) {
        int progressPercent = (int) (fileCount * DOWNLOAD_COMPLETED / ebook.getTotal());
        ebook.setDownloadProgress(progressPercent);
        if (progressPercent > 100) return;
        dataManager.updateEbookDownloadedProgress(ebook, ebook.getDownloadProgress());
        if (fileCount == ebook.getTotal()) {
            if (executor != null) {
                shutdownAndAwaitTermination(executor);
            }
            dataManager.saveNumberDownloadsEbook();
            downloadNextOrStop();
        }
        EventBus.getDefault().post(new DownloadingEvent(ebook.getId(), ebook.getDownloadProgress()));
    }
}
