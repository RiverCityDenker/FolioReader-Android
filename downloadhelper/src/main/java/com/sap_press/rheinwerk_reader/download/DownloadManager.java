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
import static com.sap_press.rheinwerk_reader.dialog.DialogCreator.createDeleteEbookInReaderDialog;
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
        updateBookDownloadTime(ebook);
        if (!isMyServiceRunning(context, DownloadService.class)) {
            DownloadService.startDownloadService(context, iconId,
                    context.getResources().getString(R.string.app_name),
                    baseUrl, appVersion, FILE_PATH_DEFAULT);
        }
    }

    public synchronized void startResume(Context context, Ebook ebook, int iconId,
                                         String appVersion, String baseUrl) {
        updateBookResumeState(ebook);
        if (!isMyServiceRunning(context, DownloadService.class)) {
            DownloadService.startDownloadService(context, iconId,
                    context.getResources().getString(R.string.app_name),
                    baseUrl, appVersion, FILE_PATH_DEFAULT);
        }
    }

    private void updateBookDownloadTime(Ebook ebook) {
        ebook.setDownloadTimeStamp(Util.getCurrentTimeStamp());
        dataManager.updateEbook(ebook);
    }

    private void updateBookResumeState(Ebook ebook) {
        ebook.setNeedResume(true);
        updateBookDownloadTime(ebook);
    }

    public void deleteEbook(Context context, Ebook ebook, GoogleAnalyticManager googleAnalyticManager,
                            EbookDeleteCallback listener, boolean isFullDelete) {

        Ebook ebookAfterUpdate = LibraryTable.getEbook(ebook.getId());
        createDeleteEbookDialog(context, ebookAfterUpdate.getTitle(), isDownloading(ebookAfterUpdate), () -> {
            googleAnalyticManager.sendEvent(AnalyticViewName.delete_download,
                                            AnalyticViewName.download_delete,
                                            ebook.getTitle(),
                                            (long) ebook.getFileSize());
            listener.deleteEbookTriggered();
            //update UI
            if (!ebookAfterUpdate.isDownloaded()) {
                EventBus.getDefault().post(new CancelDownloadEvent(ebookAfterUpdate, isFullDelete));
            }
            compositeSubscription.add(dataManager.deleteEbook(ebookAfterUpdate, isFullDelete)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::deleteEbookSuccess));
        });
    }

    public void deleteEbookFromReaderInOffline(Context context, Ebook ebook, GoogleAnalyticManager googleAnalyticManager,
                                               EbookDeleteCallback listener) {

        Ebook ebookAfterUpdate = LibraryTable.getEbook(ebook.getId());
        createDeleteEbookInReaderDialog(context, ebookAfterUpdate.getTitle(), isDownloading(ebookAfterUpdate), () -> {
            googleAnalyticManager.sendEvent(AnalyticViewName.delete_download,
                    AnalyticViewName.download_delete,
                    ebook.getTitle(),
                    (long) ebook.getFileSize());
            listener.deleteEbookTriggered();
            //update UI
            if (!ebookAfterUpdate.isDownloaded()) {
                EventBus.getDefault().post(new CancelDownloadEvent(ebookAfterUpdate, true));
            }
            compositeSubscription.add(dataManager.deleteEbook(ebookAfterUpdate, true)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::deleteEbookSuccess));
        });
    }

    public static boolean isDownloading(Ebook ebookAfterUpdate) {
        return ebookAfterUpdate.getDownloadProgress() > 0 && ebookAfterUpdate.getDownloadProgress() < 100;
    }
}
