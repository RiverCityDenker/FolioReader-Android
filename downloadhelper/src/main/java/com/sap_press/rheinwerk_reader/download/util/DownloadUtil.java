package com.sap_press.rheinwerk_reader.download.util;

import android.content.Context;
import android.content.Intent;

import com.sap_press.rheinwerk_reader.download.DownloadService;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.LibraryTable;
import com.sap_press.rheinwerk_reader.download.events.UnableDownloadEvent;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import static com.sap_press.rheinwerk_reader.utils.FileUtil.deleteDirectory;
import static com.sap_press.rheinwerk_reader.utils.FileUtil.getEbookPath;
import static com.sap_press.rheinwerk_reader.utils.Util.isMyServiceRunning;

/**
 * Created by DUY on 5/8/2018.
 */

public class DownloadUtil {

    public static final boolean ONLINE = true;
    public static final boolean OFFLINE = false;

    private static final String TAG = DownloadUtil.class.getSimpleName();

    public static String getErrorCode(Throwable throwable) {
        return throwable.getMessage();
    }

    public static void stopDownloadServiceIfNeeded(Context context, UnableDownloadEvent.DownloadErrorType errorType) {
        Intent serviceIntent = new Intent(context, DownloadService.class);
        if (context != null && isMyServiceRunning(context, DownloadService.class)) {
            context.stopService(serviceIntent);
        }
        List<Ebook> downloadedEbookList = LibraryTable.getDownloadingEbooks();
        if (!downloadedEbookList.isEmpty()) {
            for (int i = 0; i < downloadedEbookList.size(); i++) {
                Ebook ebook = downloadedEbookList.get(i);
                if (!ebook.isDownloaded()) {
                    if (ebook.getDownloadProgress() > 0) {
                        deleteDirectory(getEbookPath(context, String.valueOf(ebook.getId())));
                    }
                    ebook.resetInfo();
                    LibraryTable.updateEbook(ebook);
                }
            }
            EventBus.getDefault().post(new UnableDownloadEvent(errorType));
        }
    }

    public static void onPauseDownloads(Context context, UnableDownloadEvent.DownloadErrorType errorType) {
        Intent serviceIntent = new Intent(context, DownloadService.class);
        if (context != null && isMyServiceRunning(context, DownloadService.class)) {
            context.stopService(serviceIntent);
        }
        List<Ebook> downloadingEbookList = LibraryTable.getDownloadingEbooks();
        if (!downloadingEbookList.isEmpty()) {
            for (int i = 0; i < downloadingEbookList.size(); i++) {
                Ebook ebook = downloadingEbookList.get(i);
                ebook.setDownloadFailed(true);
                LibraryTable.updateEbook(ebook);
            }
            EventBus.getDefault().post(new UnableDownloadEvent(downloadingEbookList, errorType));
        }
    }
}
