package com.sap_press.rheinwerk_reader.download;

import android.content.Context;

import com.sap_press.rheinwerk_reader.R;
import com.sap_press.rheinwerk_reader.download.datamanager.DownloadDataManager;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.LibraryTable;
import com.sap_press.rheinwerk_reader.download.events.CancelDownloadEvent;
import com.sap_press.rheinwerk_reader.googleanalytics.AnalyticViewName;
import com.sap_press.rheinwerk_reader.googleanalytics.GoogleAnalyticManager;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.utils.Util;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.sap_press.rheinwerk_reader.dialog.DialogCreator.createDeleteEbookDialog;
import static com.sap_press.rheinwerk_reader.mod.aping.BookApi.FILE_PATH_DEFAULT;
import static com.sap_press.rheinwerk_reader.utils.Util.isMyServiceRunning;

public class DownloadManager {

    CompositeDisposable compositeSubscription;
    DownloadDataManager dataManager;

    private static final DownloadManager instance = new DownloadManager();

    public static DownloadManager getInstance() {
        return instance;
    }

    private DownloadManager() {
        dataManager = DownloadDataManager.getInstance();
        compositeSubscription = new CompositeDisposable();
    }

    public static boolean isFinishDownload(int progress) {
        return progress == 100;
    }

    public interface EbookDeleteCallback {
        void deleteEbookSuccess(Ebook ebook);

        void deleteEbookTriggered();
    }

    public synchronized void startDownload(Context context, Ebook ebook, int iconId,
                                           String appVersion, String baseUrl) {
        updateBookState(ebook);
        if (!isMyServiceRunning(context, DownloadService.class)) {
            DownloadService.startDownloadService(context, iconId,
                    context.getResources().getString(R.string.app_name),
                    baseUrl, appVersion, FILE_PATH_DEFAULT);
        }
    }

    private void updateBookState(Ebook ebook) {
        ebook.setDownloadTimeStamp(Util.getCurrentTimeStamp());
        dataManager.updateEbook(ebook);
    }

    public void deleteEbook(Context context, Ebook ebook, GoogleAnalyticManager googleAnalyticManager,
                            EbookDeleteCallback listener) {

        Ebook ebookAfterUpdate = LibraryTable.getEbook(ebook.getId());
        boolean isDownloading = ebookAfterUpdate.getDownloadProgress() > 0 && ebookAfterUpdate.getDownloadProgress() < 100;
        createDeleteEbookDialog(context, ebookAfterUpdate.getTitle(), isDownloading, () -> {
            googleAnalyticManager.sendEvent(AnalyticViewName.delete_download,
                    AnalyticViewName.download_delete,
                    ebook.getTitle(),
                    (long) ebook.getFileSize());
            listener.deleteEbookTriggered();
            //update UI
            if (!ebookAfterUpdate.isDownloaded()) {
                EventBus.getDefault().post(new CancelDownloadEvent(ebookAfterUpdate));
            }
            compositeSubscription.add(dataManager.deleteEbook(ebookAfterUpdate)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::deleteEbookSuccess));
        }, null);
    }
}
