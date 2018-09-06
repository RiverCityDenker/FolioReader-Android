package com.sap_press.rheinwerk_reader.download.datamanager;

import com.sap_press.rheinwerk_reader.download.datamanager.tables.LibraryTable;
import com.sap_press.rheinwerk_reader.download.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.download.util.Util;

import java.util.Collections;
import java.util.List;

public class DownloadDataManager {

    private static final DownloadDataManager instance = new DownloadDataManager();
    private DownloadSharedPref mDownloadSharedPref;


    public static DownloadDataManager getInstance() {
        return instance;
    }

    private DownloadDataManager() {
        mDownloadSharedPref = DownloadSharedPref.getInstance();
    }

    public void updateEbook(Ebook ebook) {
        LibraryTable.updateEbook(ebook);
    }

    public List<Ebook> getAllWaitingDownloadEbooks() {
        List<Ebook> ebookList = LibraryTable.getWaitingDownloadBooks();
        Collections.sort(ebookList, (ebook1, ebook2) -> Long.compare(ebook1.getDownloadTimeStamp(), ebook2.getDownloadTimeStamp()));
        return ebookList;
    }

    public void saveTimestampDownload(String key) {
        mDownloadSharedPref.put(DownloadSharedPref.PREF_KEY_TIME_STAMP_DOWNLOAD(key), Util.getCurrentTimeStamp());
    }

    public long getTimestampDownload(String key) {
        return mDownloadSharedPref.get(DownloadSharedPref.PREF_KEY_TIME_STAMP_DOWNLOAD(key), (long) 1526620402);
    }

    public String getAccessToken() {
        return mDownloadSharedPref.get(DownloadSharedPref.PREF_KEY_ACCESS_TOKEN, null);
    }

    public void updateEbookPath(int ebookId, String filePath) {
        LibraryTable.updateEbookPath(ebookId, filePath);
    }

    public void updateEbookDownloadedProgress(Ebook ebook, int progressPercent) {
        LibraryTable.updateEbookDownloadProgress(ebook, progressPercent);
    }

    public void saveNumberDownloadsEbook() {
        mDownloadSharedPref.put(DownloadSharedPref.PREF_KEY_NUMBER_DOWNLOAD, getNumberDownloadsEbook() + 1);
    }

    public int getNumberDownloadsEbook() {
        return mDownloadSharedPref.get(DownloadSharedPref.PREF_KEY_NUMBER_DOWNLOAD, 0);
    }

    public void saveAccessToken(String accessToken) {
        mDownloadSharedPref.put(DownloadSharedPref.PREF_KEY_ACCESS_TOKEN, accessToken);
    }

    public void saveApiKey(String apiKey) {
        mDownloadSharedPref.put(DownloadSharedPref.PREF_KEY_API_KEY, apiKey);
    }

    public String getApiKey() {
        return mDownloadSharedPref.get(DownloadSharedPref.PREF_KEY_API_KEY, null);
    }

}
