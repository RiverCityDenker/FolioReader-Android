package com.folioreader.ui.folio.presenter;

import android.content.Context;
import android.util.Log;

import com.folioreader.ui.folio.views.FolioPageMvpView;
import com.sap_press.rheinwerk_reader.download.DownloadManager;
import com.sap_press.rheinwerk_reader.download.DownloadService;
import com.sap_press.rheinwerk_reader.download.datamanager.DownloadDataManager;
import com.sap_press.rheinwerk_reader.mod.models.apiinfo.ApiInfo;
import com.sap_press.rheinwerk_reader.mod.models.downloadinfo.DownloadInfo;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

import static com.sap_press.rheinwerk_reader.utils.FileUtil.getEbookPath;

public class FolioPagePresenter {
    private static final String TAG = FolioPagePresenter.class.getSimpleName();
    private FolioPageMvpView mvpView;
    private final DownloadManager downloadManager;
    private final DownloadDataManager dataManager;

    public FolioPagePresenter(FolioPageMvpView mvpView) {
        this.mvpView = mvpView;
        downloadManager = DownloadManager.getInstance();
        dataManager = DownloadDataManager.getInstance();
    }

    public void downloadSingleFile(Context context, DownloadInfo downloadInfo, Ebook ebook, String href) {
        showLoading();
        final ApiInfo apiInfo = new ApiInfo(downloadInfo.getmBaseUrl(),
                dataManager.getAccessToken(),
                downloadInfo.getmApiKey(),
                downloadInfo.getmAppVersion());

        final String folderPath = getEbookPath(context, String.valueOf(ebook.getId()));
        new DownloadService.DownloadFileTask(context, ebook, apiInfo, folderPath, false).executeParallel(href);
    }

    private void showLoading() {
        if (this.mvpView != null)
            this.mvpView.showLoading();
    }
}
