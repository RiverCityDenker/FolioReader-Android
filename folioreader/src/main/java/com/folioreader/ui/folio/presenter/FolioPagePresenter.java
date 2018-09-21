package com.folioreader.ui.folio.presenter;

import android.content.Context;

import com.folioreader.ui.folio.views.FolioPageMvpView;
import com.sap_press.rheinwerk_reader.download.DownloadManager;
import com.sap_press.rheinwerk_reader.download.DownloadService;
import com.sap_press.rheinwerk_reader.download.datamanager.DownloadDataManager;
import com.sap_press.rheinwerk_reader.mod.models.downloadinfo.DownloadInfo;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

public class FolioPagePresenter {
    private FolioPageMvpView mvpView;
    private final DownloadManager downloadManager;
    private final DownloadDataManager dataManager;

    public FolioPagePresenter(FolioPageMvpView mvpView) {
        this.mvpView = mvpView;
        downloadManager = DownloadManager.getInstance();
        dataManager = DownloadDataManager.getInstance();
    }

    public void downloadSingleFile(Context context, DownloadInfo downloadInfo, Ebook ebook, String href) {
        new DownloadService.DownloadFileTask(context, ebook, dataManager.getAccessToken(), downloadInfo).execute(href);
    }
}
