package com.folioreader.datamanager;

import com.sap_press.rheinwerk_reader.download.datamanager.DownloadDataManager;
import com.sap_press.rheinwerk_reader.download.datamanager.DownloadSharedPref;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.LibraryTable;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;
import com.sap_press.rheinwerk_reader.utils.Util;

import java.util.Collections;
import java.util.List;

public class FolioDataManager {

    private static final FolioDataManager instance = new FolioDataManager();
    private final DownloadDataManager mDownloadDataManager;

    public static FolioDataManager getInstance() {
        return instance;
    }

    private FolioDataManager() {
        mDownloadDataManager = DownloadDataManager.getInstance();
    }

    public boolean checkEbookDownload(int ebookId) {
        return LibraryTable.checkEbookDownload(ebookId);
    }

    public boolean checkDownloadFailed(int id) {
        return LibraryTable.checkDownloadFailed(id);
    }

    public Ebook getCurrentBook(int id) {
        return LibraryTable.getEbook(id);
    }

    public int getDownloadedEbooksCount() {
        return LibraryTable.getDownloadedEbooksCount();
    }

    public String getAccessToken() {
        return mDownloadDataManager.getAccessToken();
    }
}
