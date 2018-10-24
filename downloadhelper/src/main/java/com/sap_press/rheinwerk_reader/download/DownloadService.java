package com.sap_press.rheinwerk_reader.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.sap_press.rheinwerk_reader.crypto.CryptoManager;
import com.sap_press.rheinwerk_reader.download.api.ApiClient;
import com.sap_press.rheinwerk_reader.download.api.ApiService;
import com.sap_press.rheinwerk_reader.download.datamanager.DownloadDataManager;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.LibraryTable;
import com.sap_press.rheinwerk_reader.download.events.CancelDownloadEvent;
import com.sap_press.rheinwerk_reader.download.events.DestroyDownloadServiceEvent;
import com.sap_press.rheinwerk_reader.download.events.DownloadBasicFileErrorEvent;
import com.sap_press.rheinwerk_reader.download.events.DownloadFileSuccessEvent;
import com.sap_press.rheinwerk_reader.download.events.DownloadSingleFileErrorEvent;
import com.sap_press.rheinwerk_reader.download.events.DownloadingEvent;
import com.sap_press.rheinwerk_reader.download.events.FinishDownloadContentEvent;
import com.sap_press.rheinwerk_reader.download.events.OnResetDownloadBookEvent;
import com.sap_press.rheinwerk_reader.download.events.PausedDownloadingEvent;
import com.sap_press.rheinwerk_reader.download.events.UnableDownloadEvent;
import com.sap_press.rheinwerk_reader.download.util.DownloadUtil;
import com.sap_press.rheinwerk_reader.downloadhelper.R;
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
import org.readium.r2_streamer.parser.EpubParser;
import org.readium.r2_streamer.parser.EpubParserException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
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

import static com.sap_press.rheinwerk_reader.download.events.UnableDownloadEvent.DownloadErrorType.DISCONNECTED;
import static com.sap_press.rheinwerk_reader.download.events.UnableDownloadEvent.DownloadErrorType.NOT_ENOUGH_SPACE;
import static com.sap_press.rheinwerk_reader.mod.aping.BookApi.FILE_PATH_DEFAULT;
import static com.sap_press.rheinwerk_reader.utils.Constant.X_CONTENT_KEY;
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
    private static final String IS_NETWORK_RESUME = "is_network_resume";
    static final int RETRY_COUNT = 3;
    private static final Object LOCK_OBJECT = new Object();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCancelDownloadEvent(CancelDownloadEvent event) {
        if (event.getEbook() != null) {
            if (currentEbookId == event.getEbook().getId()) {
                if (executor != null) {
                    shutdownAndAwaitTermination(executor);
                }
                Ebook ebook = event.getEbook();
                ebook.setDownloadProgress(-1);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPausedDownloading(PausedDownloadingEvent event) {
        if (executor != null) {
            shutdownAndAwaitTermination(executor);
        }
        List<Ebook> downloadingEbookList = LibraryTable.getDownloadingEbooks();
        if (!downloadingEbookList.isEmpty()) {
            for (int i = 0; i < downloadingEbookList.size(); i++) {
                Ebook ebook = downloadingEbookList.get(i);
                ebook.setDownloadFailed(true);
                LibraryTable.updateEbook(ebook);
            }
            EventBus.getDefault().post(new UnableDownloadEvent(downloadingEbookList, DISCONNECTED));
        }
        stopSelf();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDestroyDownloadServiceEvent(DestroyDownloadServiceEvent event) {
        this.stopSelf();
        onDestroy();
    }

    private void downloadNextOrStop(boolean hasErrorInPreviousBook, int errorBookId) {
        List<Ebook> ebookList;
        if (mIsNetworkResume) {
            ebookList = dataManager.getAllToResumeFromNetwork();
            if (ebookList.isEmpty()) {
                mIsNetworkResume = false;
            }
        } else {
            ebookList = dataManager.getNeedDownloadBooks();
        }
        if (!ebookList.isEmpty()) {
            Ebook currentEbook = ebookList.get(0);
            if (hasErrorInPreviousBook) {
                currentEbook = getNextBook(errorBookId, ebookList);
            }
            if (currentEbook == null) {
                destroyService();
                return;
            }
            final long availableSize = MemoryUtil.getAvailableInternalMemorySize();
            if (availableSize > currentEbook.getFileSize()) {
                updateDownloadStatusFailed(currentEbook, false);
                checkFileExist(DownloadService.this, currentEbook);
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
        EventBus.getDefault().post(new OnResetDownloadBookEvent(ebookAfterUpdate.getId(),
                ebookAfterUpdate.isDownloadFailed()));
    }

    private Ebook updateDownloadFailed(Ebook ebook, boolean isDownloadFailed) {
        ebook.setDownloadFailed(isDownloadFailed);
        dataManager.updateEbook(ebook);
        return ebook;
    }

    private Ebook updateDownloadResumeState(Ebook ebook, boolean isNeedResume) {
        ebook.setNeedResume(isNeedResume);
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
            EventBus.getDefault().post(new OnResetDownloadBookEvent(ebook.getId(),
                    ebook.isDownloadFailed()));
        }
    }

    private void checkFileExist(Context context, Ebook ebook) {
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
            if (ebookId.equalsIgnoreCase(mCurrentBookId)) return;
            mCurrentBookId = ebookId;
            sendErrorDownloadEventToGA(throwable, ebookId);

            if (executor != null) {
                shutdownAndAwaitTermination(executor);
            }
            if (isOnline(this)) {
                updateDownloadStatus(Integer.parseInt(ebookId), true, false);
                final Ebook ebook = dataManager.getEbookById(Integer.parseInt(ebookId));
                EventBus.getDefault().post(new DownloadingEvent(ebook.getId(), ebook.getDownloadProgress()));
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
            for (EpubBook.Manifest manifest : epub.manifestList) {
                if (manifest.getId().equalsIgnoreCase(CSS_ID)
                        || manifest.getId().equalsIgnoreCase(TOC_ID)) {
                    new DownloadService.DownloadFileTask(context, ebook, apiInfo, folderPath, true).execute(manifest.getHref());
                }
            }
        } else {
            long timeDownload = TimeUnit.SECONDS.toSeconds(Util.getCurrentTimeStamp() - dataManager.getTimestampDownload(ebook.getTitle()));
            googleAnalyticManager.sendEvent(AnalyticViewName.dowload_load_time, AnalyticViewName.download_duration, ebook.getTitle(), timeDownload);
            final String filePath = getEbookPath(this, String.valueOf(ebook.getId()));
            ebook.setFilePath(filePath);
            dataManager.updateEbookPath(ebook.getId(), filePath);
            downloadAllFiles(epub, apiInfo.getmToken(), ebook);
        }
    }

    private void downloadAllFiles(EpubBook epubBook, String token, Ebook ebook) {
        ebook.setTotal(epubBook.manifestList.size());
        final String ebookId = String.valueOf(ebook.getId());
        ArrayList<EpubBook.Manifest> fileListForDownload = getListFileForDownload(epubBook.manifestList, ebookId);
        progress = FileUtil.getFilesCount(ebook.getFilePath());
        int totalFileListNeedToDownload = fileListForDownload.size();
        executor = ParallelExecutorTask.createPool();
        final String folderPath = getEbookPath(this, ebookId);
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
            if (!TextUtils.isEmpty(contentKey) && !contentKey.equals(ebook.getContentKey())) {
                ebook.setContentKey(contentKey);
            }

            synchronized (DownloadService.class) {
                downloadSingleSucscess(ebook, ++progress);
            }

            return ebook;
        }

        private String downloadSingleFile(EpubBook.Manifest manifest, String fileUrl, int retryCount) {
            String contentKey = null;
            try {
                contentKey = downloadFile(fileUrl, token, folderPath, manifest.getHref(), mAppVersion);
            } catch (Exception e) {
                e.printStackTrace();
                if (isOnline(DownloadService.this)) {
                    if (--retryCount == 0) {
                        handleError(e, ebookId, getPoolExecutor(), DownloadUtil.OFFLINE);
                        if (FileUtil.isFileExist(DownloadService.this, ebookId, manifest.getHref()))
                            FileUtil.deleteFile(FileUtil.getFile(folderPath, manifest.getHref()));
                        return null;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    return downloadSingleFile(manifest, fileUrl, retryCount);
                }
            }
            return contentKey;
        }
    }


    public static class DownloadFileTask extends AsyncTask<String, Integer, Ebook> {
        private final WeakReference<Context> contextWeakReference;
        private final String appVersion;
        private final String token;
        private final String ebookId;
        private final Ebook ebook;
        private final String baseUrl;
        private final boolean isBasicData;
        private final String folderPath;
        private final String apiKey;

        public DownloadFileTask(Context context, Ebook ebook, ApiInfo apiInfo, String folderPath, boolean isBasicData) {
            this.contextWeakReference = new WeakReference<>(context);
            this.ebook = ebook;
            this.token = apiInfo.getmToken();
            this.ebookId = String.valueOf(ebook.getId());
            this.appVersion = apiInfo.getmAppVersion();
            this.baseUrl = apiInfo.getmBaseUrl();
            this.apiKey = apiInfo.getmApiKey();
            this.folderPath = folderPath;
            this.isBasicData = isBasicData;
        }

        @Override
        protected Ebook doInBackground(String... originalHrefs) {
            String originalHref = originalHrefs[0];
            final String href = FileUtil.reformatHref(originalHref);
            final String fileUrl = baseUrl + "ebooks/" + ebookId + "/download?app_version=" + appVersion + "&file_path=" + href;
            final String contentKey = downloadSingleFile(contextWeakReference.get(), fileUrl, href, appVersion, RETRY_COUNT);
            ebook.setHref(href);
            if (href.contains(".html")) {
                if (contentKey != null) {
                    final String html = CryptoManager.decryptContentKey(contentKey, apiKey, getFilePath(folderPath, originalHref));
                    try {
                        parseHtml(html);
                    } catch (EpubParserException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!TextUtils.isEmpty(contentKey) && !contentKey.equals(ebook.getContentKey())) {
                ebook.setContentKey(contentKey);
            }

            return ebook;

        }

        private String downloadSingleFile(Context context,
                                          String fileUrl,
                                          String href,
                                          String appVersion,
                                          int retryCount) {
            if (context == null) return null;
            String contentKey;
            try {
                contentKey = downloadFile(fileUrl, token, folderPath, href, appVersion);
            } catch (Exception e) {
                e.printStackTrace();
                if (--retryCount == 0) {
                    if (FileUtil.isFileExist(context, ebookId, href))
                        FileUtil.deleteFile(FileUtil.getFile(folderPath, href));
                    if (!isBasicData) {
                        onDownloadSingleFileError(context, e, ebook, isOnline(context));
                    } else {
                        onDownloadBasicFileError(context, e, ebook, isOnline(context));
                    }
                    return null;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                return downloadSingleFile(context, href, fileUrl, appVersion, retryCount);
            }
            return contentKey;
        }

        private void parseHtml(String html) throws EpubParserException {
            Document document = EpubParser.xmlParser(html);
            if (document == null) {
                throw new EpubParserException("Error while parsing");
            }
            NodeList itemNodes = document.getElementsByTagNameNS("*", "img");
            if (itemNodes != null) {
                for (int i = 0; i < itemNodes.getLength(); i++) {
                    Element itemElement = (Element) itemNodes.item(i);

                    NamedNodeMap nodeMap = itemElement.getAttributes();
                    for (int j = 0; j < nodeMap.getLength(); j++) {
                        Attr attr = (Attr) nodeMap.item(j);
                        switch (attr.getNodeName()) {
                            case "src":
                                final String src = attr.getNodeValue();
                                final String href = FileUtil.reformatHref(src);
                                try {
                                    final String fileUrl = baseUrl + "ebooks/" + ebookId
                                            + "/download?app_version=" + appVersion
                                            + "&file_path=" + href;
                                    downloadFile(fileUrl, token, folderPath, href, appVersion);
                                } catch (Exception e) {
                                    Log.e(TAG, "parseHtml:parse Image >>>" + e.getMessage());
                                }
                                break;
                        }
                    }
                }
            }

            NodeList linkNodes = document.getElementsByTagNameNS("*", "a");
            if (linkNodes != null) {
                List<String> srcList = new ArrayList<>();
                for (int i = 0; i < linkNodes.getLength(); i++) {
                    Element itemElement = (Element) linkNodes.item(i);
                    NamedNodeMap nodeMap = itemElement.getAttributes();
                    for (int j = 0; j < nodeMap.getLength(); j++) {
                        Attr attr = (Attr) nodeMap.item(j);
                        if (attr.getNodeName().equalsIgnoreCase("href")) {
                            final String src = attr.getNodeValue();
                            if (!srcList.contains(src) && src.contains(".html")) {
                                final String href = FileUtil.reformatHref(src);
                                srcList.add(src);
                                try {
                                    final String fileUrl = baseUrl + "ebooks/" + ebookId
                                            + "/download?app_version=" + appVersion
                                            + "&file_path=" + href;
                                    downloadFile(fileUrl, token, folderPath, href, appVersion);
                                } catch (Exception e) {
                                    Log.e(TAG, "parseHtml:parse Link >>>" + e.getMessage());
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        private String getFilePath(String folderPath, String originalHref) {
            folderPath = folderPath.endsWith("/")
                    ? folderPath.substring(0, folderPath.length() - 1)
                    : folderPath;
            originalHref = originalHref.startsWith("/")
                    ? originalHref.substring(1, originalHref.length())
                    : originalHref;
            Log.e(TAG, "getFilePath: >>>" + folderPath + "/" + originalHref);
            return folderPath + "/" + originalHref;
        }

        @Override
        protected void onPostExecute(Ebook ebook) {
            if (ebook == null) {
                return;
            }
            if (isBasicData) {
                if (isFileExist(contextWeakReference.get(), ebookId, "toc.ncx")
                        && isFileExist(contextWeakReference.get(), ebookId, "Styles/styles.css"))
                    EventBus.getDefault().post(new FinishDownloadContentEvent(ebook));
            } else {
                EventBus.getDefault().post(new DownloadFileSuccessEvent(ebook));
            }
        }

        private void onDownloadSingleFileError(Context context, Exception e, Ebook ebook, boolean isOnline) {
            String title;
            String message;
            if (context == null) return;
            if (isOnline) {
                title = context.getResources().getString(R.string.download_error_from_file_title);
                message = context.getResources().getString(R.string.download_error_from_file_message);
            } else {
                title = context.getResources().getString(R.string.download_error_from_offline_title);
                message = context.getResources().getString(R.string.download_error_from_offline_message);
            }
            EventBus.getDefault().post(new DownloadSingleFileErrorEvent(ebook, title, message));
        }

        private void onDownloadBasicFileError(Context context, Exception e, Ebook ebook, boolean online) {
            EventBus.getDefault().post(new DownloadBasicFileErrorEvent(e instanceof HttpException));
        }
    }

    public static String downloadFile(String fileUrl,
                                      String token,
                                      String folderPath,
                                      String href,
                                      String appVersion) throws Exception {

        File file = FileUtil.getFile(folderPath, href);
        //Log.e(TAG, "downloadFile: >>>" + file.getCanonicalPath());
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(60 * 1000);
        connection.setConnectTimeout(60 * 1000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", token);
        connection.setRequestProperty("x-project", "sap-press");
        connection.setRequestProperty("app_version", appVersion);
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

    private synchronized void downloadSingleSucscess(Ebook ebook, long fileCount) {
        final int downloadedPercent = LibraryTable.getDownloadProgressEbook(ebook.getId());

        int progressPercent = (int) (fileCount * DOWNLOAD_COMPLETED / ebook.getTotal());
        if (progressPercent > 100) return;
        if (progressPercent >= downloadedPercent) {
            ebook.setDownloadProgress(progressPercent);
            dataManager.updateEbookDownloadedProgress(ebook, ebook.getDownloadProgress());
        }

        if (fileCount == ebook.getTotal()) {
            if (executor != null) {
                shutdownAndAwaitTermination(executor);
            }
            resetDownloadState(ebook);
            dataManager.saveNumberDownloadsEbook();
            downloadNextOrStop(false, ebook.getId());
        }

        EventBus.getDefault().post(new DownloadingEvent(ebook.getId(), ebook.getDownloadProgress()));
    }

    private void resetDownloadState(Ebook ebook) {
        ebook.setDownloadFailed(false);
        ebook.setNeedResume(false);
        dataManager.updateEbook(ebook);
    }
}
