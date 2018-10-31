package com.sap_press.rheinwerk_reader.download;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.sap_press.rheinwerk_reader.download.events.DownloadBasicFileErrorEvent;
import com.sap_press.rheinwerk_reader.download.events.DownloadFileSuccessEvent;
import com.sap_press.rheinwerk_reader.download.events.DownloadSingleFileErrorEvent;
import com.sap_press.rheinwerk_reader.download.events.FinishDownloadContentEvent;
import com.sap_press.rheinwerk_reader.downloadhelper.R;
import com.sap_press.rheinwerk_reader.mod.models.apiinfo.ApiInfo;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.utils.FileUtil;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

import static com.sap_press.rheinwerk_reader.utils.FileUtil.isFileExist;
import static com.sap_press.rheinwerk_reader.utils.Util.isOnline;

public class DownloadFileTask extends ParallelExecutorTask<String, Integer, Ebook> {
    private final Context context;
    private WeakReference<Context> contextWeakReference;
    private String appVersion;
    private String token;
    private String ebookId;
    private Ebook ebook;
    private String baseUrl;
    private boolean isBasicData;
    private String folderPath;
    private String apiKey;
    private static final String TAG = DownloadFileTask.class.getSimpleName();

    public DownloadFileTask(Context context, Ebook ebook, ApiInfo apiInfo, String folderPath,
                            boolean isBasicData) {
        super(ParallelExecutorTask.createPool());
        this.contextWeakReference = new WeakReference<>(context);
        this.context = contextWeakReference.get();
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
        Log.e(TAG, "doInBackground:test >>>" + originalHref);
        final String href = FileUtil.reformatHref(originalHref);
        final String fileUrl = baseUrl + "ebooks/" + ebookId + "/download?app_version=" + appVersion + "&file_path=" + href;
        ebook.setHref(href);

        if (!isOnline(context)) {
            if (FileUtil.isFileExist(context, ebookId, href))
                FileUtil.deleteFile(FileUtil.getFile(folderPath, href));
            if (!isBasicData) {
                onDownloadSingleFileError(context, null, ebook, false);
            } else {
                onDownloadBasicFileError(context, null, ebook, false);
            }
            return null;
        }

        final String contentKey = downloadSingleFile(contextWeakReference.get(), fileUrl, href, appVersion, DownloadService.RETRY_COUNT);
        if (href.contains(".html") && !href.contains("toc.html")) {
            if (contentKey != null && !contentKey.equalsIgnoreCase(DownloadService.ERROR_DOWNLOAD_FILE)) {
                new ParseAndDownloadFileAsyn(apiKey, folderPath, originalHref,
                        baseUrl, ebookId, token, appVersion,
                        ParallelExecutorTask.createPool()).executeParallel(contentKey);
            }
        }
        if (!TextUtils.isEmpty(contentKey)
                && !contentKey.equals(ebook.getContentKey())
                && !contentKey.equalsIgnoreCase(DownloadService.ERROR_DOWNLOAD_FILE)) {
            ebook.setContentKey(contentKey);
        }
        synchronized (DownloadFileTask.class) {
            if (contentKey == null || !contentKey.equalsIgnoreCase(DownloadService.ERROR_DOWNLOAD_FILE)) {
                if (isBasicData) {
                    if (isFileExist(contextWeakReference.get(), ebookId, DownloadService.HREF_TOC)
                            && (isFileExist(contextWeakReference.get(), ebookId, DownloadService.HREF_STYLE)
                            || isFileExist(contextWeakReference.get(), ebookId, DownloadService.HREF_STYLE_COMMON))) {
                        EventBus.getDefault().post(new FinishDownloadContentEvent(ebook));
                    }
                } else {
                    EventBus.getDefault().post(new DownloadFileSuccessEvent(ebook, href));
                }
            }
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
            contentKey = HTTPDownloader.downloadFile(fileUrl, token, folderPath, href, appVersion);
        } catch (Exception e) {
            e.printStackTrace();
            synchronized (DownloadFileTask.class) {
                if (isOnline(context)) {
                    if (--retryCount == 0) {
                        if (FileUtil.isFileExist(context, ebookId, href))
                            FileUtil.deleteFile(FileUtil.getFile(folderPath, href));
                        if (!isBasicData) {
                            onDownloadSingleFileError(context, e, ebook, true);
                        } else {
                            onDownloadBasicFileError(context, e, ebook, true);
                        }
                        return DownloadService.ERROR_DOWNLOAD_FILE;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    Log.e(TAG, "downloadSingleFile: >>>retryCount = " + retryCount + ", href = " + href);
                    return downloadSingleFile(context, fileUrl, href, appVersion, retryCount);
                } else {
                    if (FileUtil.isFileExist(context, ebookId, href))
                        FileUtil.deleteFile(FileUtil.getFile(folderPath, href));
                    if (!isBasicData) {
                        onDownloadSingleFileError(context, e, ebook, false);
                    } else {
                        onDownloadBasicFileError(context, e, ebook, false);
                    }
                    return DownloadService.ERROR_DOWNLOAD_FILE;
                }
            }
        }

        return contentKey;
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