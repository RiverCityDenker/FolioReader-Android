package com.sap_press.rheinwerk_reader.download.datamanager;

import com.sap_press.rheinwerk_reader.download.datamanager.tables.LibraryTable;
import com.sap_press.rheinwerk_reader.download.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.download.util.Util;

import java.util.Collections;
import java.util.List;

public class DownloadDataManager {

    public void updateEbook(Ebook ebook) {
        LibraryTable.updateEbook(ebook);
    }

    public List<Ebook> getAllWaitingDownloadEbooks() {
        List<Ebook> ebookList = LibraryTable.getWaitingDownloadBooks();
        Collections.sort(ebookList, (ebook1, ebook2) -> Long.compare(ebook1.getDownloadTimeStamp(), ebook2.getDownloadTimeStamp()));
        return ebookList;
    }

    public void saveTimestampDownload(String key) {
        mSharedPrefsHelper.put(SharedPrefsHelper.PREF_KEY_TIME_STAMP_DOWNLOAD(key), Util.getCurrentTimeStamp());
    }

    public long getTimestampDownload(String key) {
        return mSharedPrefsHelper.get(SharedPrefsHelper.PREF_KEY_TIME_STAMP_DOWNLOAD(key), (long) 1526620402);
    }

    public String getAccessToken() {
        return mSharedPrefsHelper.get(SharedPrefsHelper.PREF_KEY_ACCESS_TOKEN, null);
    }

    public void updateEbookPath(int ebookId, String filePath) {
        LibraryTable.updateEbookPath(ebookId, filePath);
    }

    public void updateEbookDownloadedProgress(Ebook ebook, int progressPercent) {
        LibraryTable.updateEbookDownloadProgress(ebook, progressPercent);
    }

    public void saveNumberDownloadsEbook() {
        mSharedPrefsHelper.put(SharedPrefsHelper.PREF_KEY_NUMBER_DOWNLOAD, getNumberDownloadsEbook() + 1);
    }

    public int getNumberDownloadsEbook() {
        return mSharedPrefsHelper.get(SharedPrefsHelper.PREF_KEY_NUMBER_DOWNLOAD, 0);
    }

}
