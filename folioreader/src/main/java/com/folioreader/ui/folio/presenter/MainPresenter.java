package com.folioreader.ui.folio.presenter;

import android.content.Context;

import com.folioreader.ui.base.ManifestCallBack;
import com.folioreader.ui.base.ManifestTask;
import com.folioreader.ui.custom.EpubPublicationCustom;
import com.folioreader.ui.folio.views.MainMvpView;
import com.sap_press.rheinwerk_reader.download.DownloadManager;
import com.sap_press.rheinwerk_reader.download.DownloadService;
import com.sap_press.rheinwerk_reader.download.datamanager.DownloadDataManager;
import com.sap_press.rheinwerk_reader.download.events.UpdateBookUIEvent;
import com.sap_press.rheinwerk_reader.download.util.DownloadUtil;
import com.sap_press.rheinwerk_reader.googleanalytics.GoogleAnalyticManager;
import com.sap_press.rheinwerk_reader.mod.models.downloadinfo.DownloadInfo;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.utils.FileUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * @author gautam chibde on 8/6/17.
 */

public class MainPresenter implements ManifestCallBack {
    private MainMvpView mainMvpView;
    private final DownloadManager downloadManager;
    private final DownloadDataManager dataManager;

    public MainPresenter(MainMvpView mainMvpView) {
        this.mainMvpView = mainMvpView;
        downloadManager = DownloadManager.getInstance();
        dataManager = DownloadDataManager.getInstance();
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
        downloadManager.deleteEbook(context, ebook, googleAnalyticManager, new DownloadManager.EbookDeleteCallback() {
            @Override
            public void deleteEbookSuccess(Ebook ebook) {
                updateDetailUIAfterDelete(ebook);
                EventBus.getDefault().post(new UpdateBookUIEvent(currentPosition, ebook));
            }

            @Override
            public void deleteEbookTriggered() {
            }
        });
    }

    public void downloadEbook(Context context, Ebook ebook, DownloadInfo downloadInfo) {
        if (ebook.getDownloadProgress() < 0)
            ebook.setDownloadProgress(0);
        updateDownloadProgress(ebook.getId(), ebook.getDownloadProgress());
        EventBus.getDefault().post(new UpdateBookUIEvent(downloadInfo.getmBookPosition(), ebook));
        downloadManager.startDownload(context,
                ebook,
                downloadInfo.getmDownloadIcon(),
                downloadInfo.getmAppVersion(),
                downloadInfo.getmBaseUrl());
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

    public void deleteCacheData(Ebook ebook) {
        FileUtil.deleteDownloadedEbookFromExternalStorage(ebook);
    }
}
