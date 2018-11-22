package com.sap_press.rheinwerk_reader.download.datamanager;

import android.content.Context;
import android.util.Log;

import com.sap_press.rheinwerk_reader.download.datamanager.tables.LibraryTable;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.utils.FileUtil;
import com.sap_press.rheinwerk_reader.utils.Util;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class DownloadDataManager {

    private static final DownloadDataManager instance = new DownloadDataManager();
    private DownloadSharedPref mDownloadSharedPref;
    private CompositeDisposable compositeDisposable;


    public static DownloadDataManager getInstance() {
        return instance;
    }

    private DownloadDataManager() {
        mDownloadSharedPref = DownloadSharedPref.getInstance();
        compositeDisposable = new CompositeDisposable();
    }

    public void updateEbook(Ebook ebook) {
        LibraryTable.updateEbook(ebook);
    }

    public List<Ebook> getNeedDownloadBooks() {
        List<Ebook> ebookList = LibraryTable.getNeedDownloadBooks();
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

    public Observable<Ebook> deleteEbook(Context context, Ebook ebook, boolean isFullDelete) {
        return Observable.fromCallable(() -> {
            boolean isDownloading = ebook.isDownloading();
            String filePath = LibraryTable.getEbook(ebook.getId()).getFilePath();
            ebook.setFilePath(filePath);
            FileUtil.deleteDownloadedEbookFromExternalStorage(context, ebook, isFullDelete);
            if (isFullDelete)
                ebook.resetInfo();
            else
                ebook.resetApartInfo();
            LibraryTable.updateEbook(ebook);
            if (isDownloading) {
                Log.e("DownloadDataManager", "deleteEbook: >>>book is downloading ...");
                Thread.sleep(6000);
            }
            return ebook;
        });
    }

    public void updateDownloadedEbook(Ebook ebook) {
        LibraryTable.updateEbook(ebook);
    }

    public void setOnlineOfflineReading(boolean isOnlineReading) {
        mDownloadSharedPref.put(DownloadSharedPref.PREF_KEY_READING_TYPE_KEY, isOnlineReading);
    }

    public boolean isOnlineReading() {
        return mDownloadSharedPref.get(DownloadSharedPref.PREF_KEY_READING_TYPE_KEY, true);
    }

    public Ebook getEbookById(int ebookId) {
        return LibraryTable.getEbook(ebookId);
    }

    public List<Ebook> getAllToResumeFromNetwork() {
        final List<Ebook> ebookList = LibraryTable.getEbooksNeedToResumeFromNetwork();
        Collections.sort(ebookList, (ebook1, ebook2) -> Long.compare(ebook1.getDownloadTimeStamp(), ebook2.getDownloadTimeStamp()));
        return ebookList;
    }
}
