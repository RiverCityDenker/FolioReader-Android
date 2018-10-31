package com.sap_press.rheinwerk_reader.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.sap_press.rheinwerk_reader.download.api.ApiClient;
import com.sap_press.rheinwerk_reader.download.api.ApiService;
import com.sap_press.rheinwerk_reader.download.datamanager.DownloadDataManager;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.LibraryTable;
import com.sap_press.rheinwerk_reader.download.events.CancelDownloadEvent;
import com.sap_press.rheinwerk_reader.download.events.DestroyDownloadServiceEvent;
import com.sap_press.rheinwerk_reader.download.events.DownloadBasicFileErrorEvent;
import com.sap_press.rheinwerk_reader.download.events.DownloadingEvent;
import com.sap_press.rheinwerk_reader.download.events.FinishDownloadContentEvent;
import com.sap_press.rheinwerk_reader.download.events.OnDownloadInterruptedBookEvent;
import com.sap_press.rheinwerk_reader.download.events.OnResetDownloadBookEvent;
import com.sap_press.rheinwerk_reader.download.events.PausedDownloadingEvent;
import com.sap_press.rheinwerk_reader.download.events.UnableDownloadEvent;
import com.sap_press.rheinwerk_reader.download.util.DownloadUtil;
import com.sap_press.rheinwerk_reader.googleanalytics.AnalyticViewName;
import com.sap_press.rheinwerk_reader.googleanalytics.GoogleAnalyticManager;
import com.sap_press.rheinwerk_reader.mod.models.apiinfo.ApiInfo;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.mod.models.foliosupport.EpubBook;
import com.sap_press.rheinwerk_reader.utils.FileUtil;
import com.sap_press.rheinwerk_reader.utils.MemoryUtil;
import com.sap_press.rheinwerk_reader.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.sap_press.rheinwerk_reader.download.events.UnableDownloadEvent.DownloadErrorType.DISCONNECTED;
import static com.sap_press.rheinwerk_reader.download.events.UnableDownloadEvent.DownloadErrorType.NOT_ENOUGH_SPACE;
import static com.sap_press.rheinwerk_reader.mod.aping.BookApi.FILE_PATH_DEFAULT;
import static com.sap_press.rheinwerk_reader.utils.FileUtil.getEbookPath;
import static com.sap_press.rheinwerk_reader.utils.FileUtil.isFileExist;
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
    private static final String CSS_ID = "css";
    private static final String TOC_ID = "ncx";
    private static final String COVER_ID = "cover";
    private static final String COVER_IMAGE_ID = "cover-image";
    public static final String HREF_TOC = "toc.ncx";
    public static final String HREF_STYLE = "Styles/styles.css";
    public static final String HREF_STYLE_COMMON = "common/styles.css";
    private static final String IS_NETWORK_RESUME = "is_network_resume";
    public static final String ERROR_DOWNLOAD_FILE = "error_download_file";
    private static final int CONTENT_KEY_LENGTH = 16;
    public static final int RETRY_COUNT = 3;
    private static final Object LOCK_OBJECT = new Object();
    private static final int NUMBER_OF_BASIC_FILE = 4;
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
    private boolean mIsNetworkResume;
    private List<String> failedDownloadFiles = new ArrayList<>();
    private boolean mIsTryAgain;

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

    public static void startResumeDownloadService(Context context, int iconId, String title,
                                                  String baseUrl, String appVersion, String contentFile,
                                                  boolean isNetworkResume) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(NOTIFICATION_ICON, iconId);
        intent.putExtra(NOTIFICATION_TITLE, title);
        intent.putExtra(BASE_URL_KEY, baseUrl);
        intent.putExtra(APP_VERSION_KEY, appVersion);
        intent.putExtra(CONTENT_FILE_PATH, contentFile);
        intent.putExtra(IS_NETWORK_RESUME, isNetworkResume);
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
            mIsNetworkResume = intent.getBooleanExtra(IS_NETWORK_RESUME, false);
            mApiService = ApiClient.getClient(this, mBaseUrl).create(ApiService.class);

            String channelId = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel();
                channelId = "my_service";
            }

            final Notification notification = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(mIconId)
                    .setContentTitle(mTitle)
                    .setContentText("Downloading...").build();

            startForeground(SERVICE_FOREGROUND_ID, notification);
            downloadNextOrStop(false, 0);
        }
        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel chan = new NotificationChannel("my_service",
                "My Background Service", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        compositeSubscription.dispose();
        stopForeground(true);
        this.stopSelf();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onCancelDownloadEvent(CancelDownloadEvent event) {
        if (event.getEbook() != null) {
            if (currentEbookId == event.getEbook().getId()) {
                if (executor != null) {
                    shutdownAndAwaitTermination(executor);
                }
                Ebook ebook = event.getEbook();
                if (event.isFullDelete())
                    ebook.resetInfo();
                else
                    ebook.resetApartInfo();
                dataManager.updateEbook(ebook);
                downloadNextOrStop(false, 0);
            } else {
                Log.e(TAG, "onCancelDownloadEvent: >>>");
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

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onPausedDownloading(PausedDownloadingEvent event) {
        if (executor != null) {
            shutdownAndAwaitTermination(executor);
        }
        List<Ebook> downloadingEbookList = LibraryTable.getDownloadingEbooks();
        if (!downloadingEbookList.isEmpty()) {
            for (int i = 0; i < downloadingEbookList.size(); i++) {
                Ebook ebook = downloadingEbookList.get(i);
                ebook.setDownloadFailed(true);
                ebook.setNeedResume(true);
                LibraryTable.updateEbook(ebook);
            }
            EventBus.getDefault().post(new UnableDownloadEvent(downloadingEbookList, DISCONNECTED));
        }
        stopSelf();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onDestroyDownloadServiceEvent(DestroyDownloadServiceEvent event) {
        this.stopSelf();
        onDestroy();
    }

    private void downloadNextOrStop(boolean hasErrorInPreviousBook, int errorBookId) {
        List<Ebook> ebookList = new ArrayList<>();
        if (mIsNetworkResume) {
            ebookList = dataManager.getAllToResumeFromNetwork();
            if (ebookList.isEmpty()) {
                mIsNetworkResume = false;
            } else {
                for (Ebook ebook : ebookList) {
                    updateDownloadStatusFailed(ebook, false);
                }
            }
        }
        if (!mIsNetworkResume) {
            ebookList = dataManager.getNeedDownloadBooks();
        }
        if (!ebookList.isEmpty()) {
            Ebook currentEbook = ebookList.get(0);
            if (hasErrorInPreviousBook) {
                currentEbook = getNextBook(errorBookId, ebookList);
            }
            if (currentEbook == null || currentEbook.getId() == currentEbookId) {
                destroyService();
                return;
            }
            final long availableSize = MemoryUtil.getAvailableInternalMemorySize();
            if (availableSize > currentEbook.getFileSize()) {
                updateDownloadStatusFailed(currentEbook, false);
                startDownloadIfNotExist(DownloadService.this, currentEbook);
            } else {
                DownloadUtil.stopDownloadServiceIfNeeded(this, NOT_ENOUGH_SPACE);
            }
        } else {
            this.stopSelf();
        }
    }

    private void destroyService() {
        if (executor != null) {
            shutdownAndAwaitTermination(executor);
        }
        this.stopSelf();
    }

    private Ebook getNextBook(int errorBookId, List<Ebook> ebookList) {
        Ebook currentEbook = null;
        for (Ebook ebook : ebookList) {
            if (ebook.getId() != errorBookId) {
                currentEbook = ebook;
                break;
            }
        }
        return currentEbook;
    }

    private void updateDownloadStatusFailed(Ebook ebook, boolean isDownloadFailed) {
        final Ebook ebookAfterUpdate = updateDownloadFailed(ebook, isDownloadFailed);
        EventBus.getDefault().post(new OnResetDownloadBookEvent(ebookAfterUpdate));
    }

    private Ebook updateDownloadFailed(Ebook ebook, boolean isDownloadFailed) {
        ebook.setDownloadFailed(isDownloadFailed);
        dataManager.updateEbook(ebook);
        return ebook;
    }

    private Ebook updateDownloadStatus(Ebook ebook, boolean isDownloadFailed, boolean isNeedResume) {
        ebook.setDownloadFailed(isDownloadFailed);
        ebook.setNeedResume(isNeedResume);
        dataManager.updateEbook(ebook);
        return ebook;
    }

    private void updateDownloadStatus(int ebookId, boolean isDownloadFailed, boolean isNeedResume) {
        synchronized (DownloadService.class) {
            Ebook ebook = dataManager.getEbookById(ebookId);
            ebook = updateDownloadStatus(ebook, isDownloadFailed, isNeedResume);
            EventBus.getDefault().post(new OnResetDownloadBookEvent(ebook));
            EventBus.getDefault().post(new OnDownloadInterruptedBookEvent(ebook));
        }
    }

    private void startDownloadIfNotExist(Context context, Ebook ebook) {
        currentEbookId = ebook.getId();
        if (!isFileExist(context, String.valueOf(ebook.getId()), mContentFileDefault)) {
            downloadEbook(ebook);
        } else {
            final String folderPath = getEbookPath(context, String.valueOf(ebook.getId()));
            final String contentPath = folderPath + "/" + mContentFileDefault;
            final String token = dataManager.getAccessToken();
            new FileUtil.ContentParserAsyn(epubBook -> {
                downloadAllFiles(epubBook, token, ebook);
            }).execute(contentPath);
        }
    }

    public void downloadEbook(Ebook ebook) {
        dataManager.updateEbook(ebook);
        googleAnalyticManager.sendEvent(AnalyticViewName.start_download_start,
                AnalyticViewName.download_start, ebook.getTitle(), (long) ebook.getFileSize());
        dataManager.saveTimestampDownload(ebook.getTitle());
        final String token = dataManager.getAccessToken();
        downloadContent(this, ebook, token, mAppVersion, mBaseUrl, DownloadUtil.OFFLINE);
    }

    public void downloadContent(Context context,
                                Ebook ebook,
                                String token,
                                String mAppVersion,
                                String mBaseUrl,
                                boolean isOnlineReading) {
        final String ebookId = String.valueOf(ebook.getId());
        if (mApiService == null)
            mApiService = ApiClient.getClient(this, mBaseUrl).create(ApiService.class);
        if (dataManager == null)
            dataManager = DownloadDataManager.getInstance();
        final ApiInfo apiInfo = new ApiInfo(mBaseUrl, token, dataManager.getApiKey(), mAppVersion);
        if (isOnlineReading) {
            if (!isFileExist(context, String.valueOf(ebook.getId()), mContentFileDefault)) {
                composeSubscription(context, ebook, token, mAppVersion, isOnlineReading, ebookId, apiInfo);
            } else {
                final String folderPath = getEbookPath(context, String.valueOf(ebook.getId()));
                final String contentPath = folderPath + "/" + mContentFileDefault;
                new FileUtil.ContentParserAsyn(epubBook -> {
                    downloadContentSuccess(context, ebook, epubBook, apiInfo, isOnlineReading);
                }).execute(contentPath);
            }
        } else {
            composeSubscription(context, ebook, token, mAppVersion, isOnlineReading, ebookId, apiInfo);
        }
    }

    private void composeSubscription(Context context, Ebook ebook, String token, String mAppVersion, boolean isOnlineReading, String ebookId, ApiInfo apiInfo) {
        final Disposable subscription = mApiService.download(ebookId, token, mAppVersion, mAppVersion, mContentFileDefault)
                .map(responseBody -> FileUtil.writeResponseBodyToDisk(context, responseBody, ebookId, mContentFileDefault))
                .map(FileUtil::parseContentFileToObject)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(o -> downloadContentSuccess(context, ebook, o, apiInfo, isOnlineReading), throwable -> {
                    handleError(throwable, ebookId, null, isOnlineReading);
                });

        if (compositeSubscription == null)
            compositeSubscription = new CompositeDisposable();
        compositeSubscription.add(subscription);
    }

    private void handleError(Throwable throwable, String ebookId, ThreadPoolExecutor executor, boolean isOnlineReading) {
        if (!isOnlineReading) {
            Log.e(TAG, "handleError: >>>" + ebookId + " - " + currentEbookId);
            if (ebookId.equalsIgnoreCase(mCurrentBookId)) return;
            mCurrentBookId = ebookId;
            sendErrorDownloadEventToGA(throwable, ebookId);

            if (executor != null) {
                shutdownAndAwaitTermination(executor);
            }
            if (isOnline(this)) {
                Log.e(TAG, "handleError: >>>isOnline");
                updateDownloadStatus(Integer.parseInt(ebookId), true, false);
                final Ebook ebook = dataManager.getEbookById(Integer.parseInt(ebookId));
                EventBus.getDefault().post(new DownloadingEvent(ebook));
                downloadNextOrStop(true, Integer.parseInt(ebookId));
            } else {
                stopSelf();
            }
            // Comment this line because this ticket : https://2denker.atlassian.net/browse/RE-431
            //listener.handleDownloadError(throwable);
        } else {
            Log.e(TAG, "handleError: >>>" + throwable.getMessage());
            EventBus.getDefault().post(new DownloadBasicFileErrorEvent(throwable instanceof HttpException));
        }

    }

    private void sendErrorDownloadEventToGA(Throwable throwable, String ebookId) {
        String url = mBaseUrl + "ebooks/" + ebookId + "/download";
        googleAnalyticManager.sendEvent(AnalyticViewName.download_error, url, DownloadUtil.getErrorCode(throwable));
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

    private void downloadContentSuccess(Context context, Ebook ebook, Object object, ApiInfo apiInfo, boolean isOnlineReading) {
        final EpubBook epub = (EpubBook) object;
        final String ebookId = String.valueOf(ebook.getId());
        if (isOnlineReading) {
            final String filePath = getEbookPath(context, String.valueOf(ebook.getId()));
            ebook.setFilePath(filePath);
            if (dataManager == null)
                dataManager = DownloadDataManager.getInstance();
            dataManager.updateEbookPath(ebook.getId(), filePath);
            final String folderPath = getEbookPath(context, ebookId);

            //deleteBasicFilesBeforeDownload(context, ebookId, folderPath);

            int count = NUMBER_OF_BASIC_FILE;
            for (EpubBook.Manifest manifest : epub.manifestList) {
                if (manifest.getId().equalsIgnoreCase(CSS_ID) || manifest.getId().equalsIgnoreCase(TOC_ID)
                        || manifest.getId().equalsIgnoreCase(COVER_ID) || manifest.getId().equalsIgnoreCase(COVER_IMAGE_ID)) {
                    count--;
                    if (!isFileExist(context, String.valueOf(ebook.getId()), manifest.getHref())) {
                        new DownloadFileTaskSync(context, ebook, apiInfo, folderPath, true).downloadSync(manifest.getHref());
                    }
                    if (count == 0) break;
                }
            }
            EventBus.getDefault().post(new FinishDownloadContentEvent(ebook));

        } else {
            long timeDownload = TimeUnit.SECONDS.toSeconds(Util.getCurrentTimeStamp() - dataManager.getTimestampDownload(ebook.getTitle()));
            googleAnalyticManager.sendEvent(AnalyticViewName.dowload_load_time, AnalyticViewName.download_duration, ebook.getTitle(), timeDownload);
            final String filePath = getEbookPath(this, String.valueOf(ebook.getId()));
            ebook.setFilePath(filePath);
            dataManager.updateEbookPath(ebook.getId(), filePath);
            downloadAllFiles(epub, apiInfo.getmToken(), ebook);
        }
    }

    private void deleteBasicFilesBeforeDownload(Context context, String ebookId, String folderPath) {
        if (FileUtil.isFileExist(context, ebookId, HREF_TOC))
            FileUtil.deleteFile(FileUtil.getFile(folderPath, HREF_TOC));
        if (FileUtil.isFileExist(context, ebookId, HREF_STYLE))
            FileUtil.deleteFile(FileUtil.getFile(folderPath, HREF_STYLE));
        if (FileUtil.isFileExist(context, ebookId, HREF_STYLE_COMMON))
            FileUtil.deleteFile(FileUtil.getFile(folderPath, HREF_STYLE_COMMON));
    }

    private void downloadAllFiles(EpubBook epubBook, String token, Ebook ebook) {
        ebook.setTotal(epubBook.manifestList.size());
        final String ebookId = String.valueOf(ebook.getId());
        ArrayList<EpubBook.Manifest> fileListForDownload = getListFileForDownload(epubBook.manifestList, ebookId);
        progress = FileUtil.getFilesCount(ebook.getFilePath());
        int totalFileListNeedToDownload = fileListForDownload.size();
        executor = ParallelExecutorTask.createPool();
        final String folderPath = getEbookPath(this, ebookId);
        mIsTryAgain = totalFileListNeedToDownload > 0 && totalFileListNeedToDownload < ebook.getTotal();
        if (totalFileListNeedToDownload > 0) {
            for (int i = 0; i < totalFileListNeedToDownload; i++) {
                final EpubBook.Manifest manifest = fileListForDownload.get(i);
                DownloadFileAsyn downloadFileAsyn = new DownloadFileAsyn(ebook, folderPath, token, i, executor);
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
        private String folderPath;

        DownloadFileAsyn(Ebook ebook, String folderPath, String token, int name, ThreadPoolExecutor executor) {
            super(executor);
            this.ebook = ebook;
            this.folderPath = folderPath;
            this.token = token;
            this.ebookId = String.valueOf(ebook.getId());
            this.name = name;
        }

        @Override
        protected Ebook doInBackground(EpubBook.Manifest... manifests) {
            if (isStop()) return null;
            executor = getPoolExecutor();
            EpubBook.Manifest manifest = manifests[0];
            String href = manifest.getHref();
            final String fileUrl = mBaseUrl + "ebooks/" + ebookId + "/download?app_version=" + mAppVersion + "&file_path=" + href;
            final String contentKey = downloadSingleFile(manifest, fileUrl, RETRY_COUNT);
            if (isStop()) return null;
            if (!TextUtils.isEmpty(contentKey)
                    && !contentKey.equals(ebook.getContentKey())
                    && !contentKey.equalsIgnoreCase(ERROR_DOWNLOAD_FILE)) {
                ebook.setContentKey(contentKey);
            }

            synchronized (DownloadService.class) {
                if (contentKey != null) {
                    if (!contentKey.equalsIgnoreCase(ERROR_DOWNLOAD_FILE)) {
                        downloadSingleSucscess(ebook, ++progress);
                    } else {
                        downloadSingleSucscess(ebook, progress);
                    }
                } else {
                    downloadSingleSucscess(ebook, ++progress);
                }
            }

            return ebook;
        }

        private String downloadSingleFile(EpubBook.Manifest manifest, String fileUrl, int retryCount) {
            String contentKey = null;
            try {
                contentKey = HTTPDownloader.downloadFile(fileUrl, token, folderPath, manifest.getHref(), mAppVersion);
            } catch (Exception e) {
                e.printStackTrace();
                if (isOnline(DownloadService.this)) {
                    failedDownloadFiles.add(manifest.getHref());
                    return ERROR_DOWNLOAD_FILE;
                }
            }
            return contentKey;
        }
    }

    private ArrayList<EpubBook.Manifest> getListFileForDownload(ArrayList<EpubBook.Manifest> manifestList, String ebookId) {
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
        String folderPath = getEbookPath(DownloadService.this, ebookId);
        for (EpubBook.Manifest manifest : manifestList) {
            if (manifest.getHref().contains("html") && FileUtil.isFileExist(this, ebookId, manifest.getHref()) && deleteCount < 5) {
                deleteCount++;
                FileUtil.deleteFile(FileUtil.getFile(folderPath, manifest.getHref()));
            }
        }
    }

    private void downloadSingleSucscess(Ebook ebook, long fileCount) {
        synchronized (DownloadService.class) {
            final int downloadedPercent = LibraryTable.getDownloadProgressEbook(ebook.getId());

            int progressPercent = (int) (fileCount * DOWNLOAD_COMPLETED / ebook.getTotal());
            if (progressPercent > 100 || downloadedPercent < 0) return;
            if (progressPercent >= downloadedPercent) {
                ebook.setDownloadProgress(progressPercent);
                dataManager.updateEbookDownloadedProgress(ebook, ebook.getDownloadProgress());
            }

            if ((fileCount + failedDownloadFiles.size()) == ebook.getTotal()) {
                Log.e(TAG, "downloadSingleSucscess:2 >>>" + fileCount + " : " + failedDownloadFiles.size() + " : " + ebook.getTotal());
                if (!failedDownloadFiles.isEmpty()) {
                    if (executor != null) {
                        shutdownAndAwaitTermination(executor);
                    }
                    int successProgressPercent = DOWNLOAD_COMPLETED - ((failedDownloadFiles.size() * DOWNLOAD_COMPLETED) / ebook.getTotal() + 1);
                    ebook.setDownloadFailed(true);
                    ebook.setDownloadProgress(successProgressPercent);
                    ebook.setNeedResume(false);
                    dataManager.updateEbook(ebook);
                    downloadNextOrStop(true, ebook.getId());
                    EventBus.getDefault().post(new OnDownloadInterruptedBookEvent(ebook));
                } else {
                    if (executor != null) {
                        shutdownAndAwaitTermination(executor);
                    }
                    resetDownloadState(ebook);
                    dataManager.saveNumberDownloadsEbook();
                    downloadNextOrStop(false, ebook.getId());
                }
            } else if ((fileCount + failedDownloadFiles.size()) > ebook.getTotal()) {
                if (!failedDownloadFiles.isEmpty()) {
                    int successProgressPercent = DOWNLOAD_COMPLETED - ((failedDownloadFiles.size() * DOWNLOAD_COMPLETED) / ebook.getTotal() + 1);
                    ebook.setDownloadFailed(true);
                    ebook.setDownloadProgress(successProgressPercent);
                    ebook.setNeedResume(false);
                    dataManager.updateEbook(ebook);
                    resetDownloadFailedByFile();
                    downloadNextOrStop(true, ebook.getId());
                    EventBus.getDefault().post(new OnDownloadInterruptedBookEvent(ebook));
                }
                EventBus.getDefault().post(new DownloadingEvent(ebook));
                return;
            }

            EventBus.getDefault().post(new DownloadingEvent(ebook));
        }
    }

    private void resetDownloadState(Ebook ebook) {
        ebook.setDownloadFailed(false);
        ebook.setNeedResume(false);
        dataManager.updateEbook(ebook);
    }

    private void resetDownloadFailedByFile() {
        failedDownloadFiles.clear();
    }
}
