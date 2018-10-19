package com.folioreader.ui.folio.presenter;

import android.content.Context;

import com.folioreader.R;
import com.folioreader.datamanager.FolioDataManager;
import com.folioreader.ui.base.ManifestCallBack;
import com.folioreader.ui.base.ManifestTask;
import com.folioreader.ui.custom.EpubPublicationCustom;
import com.folioreader.ui.folio.views.MainMvpView;
import com.sap_press.rheinwerk_reader.dialog.DialogCreator;
import com.sap_press.rheinwerk_reader.download.DownloadManager;
import com.sap_press.rheinwerk_reader.download.DownloadService;
import com.sap_press.rheinwerk_reader.download.datamanager.DownloadDataManager;
import com.sap_press.rheinwerk_reader.download.events.UpdateBookUIEvent;
import com.sap_press.rheinwerk_reader.download.util.DownloadUtil;
import com.sap_press.rheinwerk_reader.googleanalytics.AnalyticViewName;
import com.sap_press.rheinwerk_reader.googleanalytics.GoogleAnalyticManager;
import com.sap_press.rheinwerk_reader.mod.models.downloadinfo.DownloadInfo;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.utils.Constant;
import com.sap_press.rheinwerk_reader.utils.FileUtil;
import com.sap_press.rheinwerk_reader.utils.Util;

import org.greenrobot.eventbus.EventBus;

import static com.sap_press.rheinwerk_reader.dialog.DialogCreator.createMessageDialog;
import static com.sap_press.rheinwerk_reader.dialog.DialogCreator.createPausedDownloadDialog;
import static com.sap_press.rheinwerk_reader.utils.Util.isOnline;

/**
 * @author gautam chibde on 8/6/17.
 */

public class MainPresenter implements ManifestCallBack {
    private final DownloadInfo mDownloadInfo;
    private final GoogleAnalyticManager mGoogleAnalytics;
    private final DownloadManager downloadManager;
    private final DownloadDataManager dataManager;
    private final MainMvpView mainMvpView;
    private Ebook mEbook;
    private final FolioDataManager folioDataManager;

    public MainPresenter(MainMvpView mainMvpView, Ebook ebook, DownloadInfo downloadInfo, GoogleAnalyticManager googleAnalytic) {
        this.mainMvpView = mainMvpView;
        mEbook = ebook;
        mDownloadInfo = downloadInfo;
        mGoogleAnalytics = googleAnalytic;
        downloadManager = DownloadManager.getInstance();
        dataManager = DownloadDataManager.getInstance();
        folioDataManager = FolioDataManager.getInstance();
    }

    public void parseManifest(String url) {
        new ManifestTask(this).execute(url);
    }

    @Override
    public void onReceivePublication(EpubPublicationCustom publication) {
        mainMvpView.onLoadPublication(publication);
    }

    @Override
    public void onError() {
        mainMvpView.onError();
    }


    public void deleteEbook(Context context, Ebook ebook,
                            GoogleAnalyticManager googleAnalyticManager, int currentPosition) {
        sendEventActionGoogleAnalytics("Selected delete book");
        if (Util.isOnline(context)) {
            downloadManager.deleteEbook(context, ebook, googleAnalyticManager, new DownloadManager.EbookDeleteCallback() {
                @Override
                public void deleteEbookSuccess(Ebook ebook) {
                    hideLoading();
                    updateDetailUIAfterDelete(ebook);
                    EventBus.getDefault().post(new UpdateBookUIEvent(currentPosition, ebook));
                }

                @Override
                public void deleteEbookTriggered() {
                    showLoading();
                }
            });
        } else {
            downloadManager.deleteEbookFromReaderInOffline(context, ebook, googleAnalyticManager,
                    new DownloadManager.EbookDeleteCallback() {
                        @Override
                        public void deleteEbookSuccess(Ebook ebook) {
                            updateDetailUIAfterDelete(ebook);
                            EventBus.getDefault().post(new UpdateBookUIEvent(currentPosition, ebook));
                            if (mainMvpView != null)
                                mainMvpView.exitReader();
                        }

                        @Override
                        public void deleteEbookTriggered() {
                            showLoading();
                        }
                    });
        }
    }

    public void downloadEbook(Context context, Ebook ebook) {
        if (ebook.getDownloadProgress() < 0)
            ebook.setDownloadProgress(0);
        updateDownloadProgress(ebook.getId(), ebook.getDownloadProgress());
        EventBus.getDefault().post(new UpdateBookUIEvent(mDownloadInfo.getmBookPosition(), ebook));
        downloadManager.startDownload(context,
                ebook,
                mDownloadInfo.getmDownloadIcon(),
                mDownloadInfo.getmAppVersion(),
                mDownloadInfo.getmBaseUrl());
    }

    private void updateDetailUIAfterDelete(Ebook ebook) {
        if (mainMvpView != null) {
            mainMvpView.updateUIAfterDelete(ebook);
        }
    }

    private void updateDownloadProgress(int id, int downloadProgress) {
        if (mainMvpView != null) {
            mainMvpView.updateDownloadProgress(id, downloadProgress);
        }
    }

    public void downloadContent(Context context, Ebook mEbook, DownloadInfo downloadInfo) {
        showLoading();
        new DownloadService().downloadContent(context,
                mEbook,
                dataManager.getAccessToken(),
                downloadInfo.getmAppVersion(),
                downloadInfo.getmBaseUrl(),
                DownloadUtil.ONLINE);
    }

    private void showLoading() {
        if (mainMvpView != null)
            mainMvpView.showLoading();
    }

    private void hideLoading() {
        if (mainMvpView != null)
            mainMvpView.hideLoading();
    }

    public void deleteCacheData(Ebook ebook) {
        FileUtil.deleteDownloadedEbookFromExternalStorage(ebook);
    }

    public void handleClickOnDownloadingView(Context context, Ebook ebook) {
        mEbook = ebook;
        boolean isDownloadEbook = folioDataManager.checkEbookDownload(this.mEbook.getId());
        Ebook currentEbook = folioDataManager.getCurrentBook(this.mEbook.getId());
        if (folioDataManager.getDownloadedEbooksCount() >= Constant.MAXIMUM_DOWNLOAD_NUMBER && !isDownloadEbook) {
            createMessageDialog(context, context.getString(R.string.limit_title), context.getString(R.string.limit_message));
        } else if (!isOnline(context) && currentEbook.getDownloadProgress() < 0) {
            createMessageDialog(context, context.getString(R.string.download_offline_title),
                    context.getString(R.string.download_offline_message),
                    context.getString(R.string.close));
        } else {
            if (isDownloadEbook) {
                final boolean isDownloadFailed = folioDataManager.checkDownloadFailed(this.mEbook.getId());
                if (isDownloadFailed) {
                    handlePausedDownload(context, currentEbook);
                } else {
                    handleAbortDownload(context, currentEbook);
                }
            } else {
                startDownloadBook(context, currentEbook);
            }
        }
    }

    private void startDownloadBook(Context context, Ebook ebook) {
        sendEventActionGoogleAnalytics("Selected download book");
        downloadEbook(context, ebook);
    }

    private void sendEventActionGoogleAnalytics(String action) {
        mGoogleAnalytics.sendEvent(AnalyticViewName.Reader, action, "Screen Reader");
    }


    private void handleAbortDownload(Context context, Ebook mEbook) {
        deleteEbook(context, mEbook, mGoogleAnalytics, mDownloadInfo.getmBookPosition());
    }

    private void handlePausedDownload(Context context, Ebook ebook) {
        createPausedDownloadDialog(context, new DialogCreator.PausedDialogCallback() {
            @Override
            public void onAbort() {
                handleAbortDownload(context, ebook);
            }

            @Override
            public void onResume() {
                resumeEbook(context, ebook);
            }
        });
    }

    private void resumeEbook(Context context, Ebook ebook) {
        if (!isOnline(context)) {
            createMessageDialog(context,
                    context.getString(R.string.download_offline_title),
                    context.getString(R.string.download_offline_message),
                    context.getString(R.string.close));
        } else {
            String baseUrl = mDownloadInfo.getmBaseUrl();
            downloadManager.startResume(context,
                    ebook,
                    mDownloadInfo.getmDownloadIcon(),
                    mDownloadInfo.getmAppVersion(),
                    baseUrl);
        }
    }

}
