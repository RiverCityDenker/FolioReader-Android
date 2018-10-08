package com.sap_press.rheinwerk_reader.download.datamanager.tables;

import android.text.TextUtils;

import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

import java.util.List;

import static com.sap_press.rheinwerk_reader.download.datamanager.tables.EbookTable.*;


public class LibraryTable {
    public static String TABLE_NAME = "ebooks";

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TITLE + " VARCHAR(50), "
            + COLUMN_SUBTITLE + " VARCHAR(100), "
            + COLUMN_LAST_READ + " TEXT, "
            + COLUMN_LINKS + " TEXT, "
            + COLUMN_ESBN + " VARCHAR(50), "
            + COLUMN_IS_STANDALONE + " VARCHAR(10), "
            + COLUMN_CLAIM + " VARCHAR(100), "
            + COLUMN_EDITION_TEXT + " TEXT, "
            + COLUMN_PAGE_NUMBER + " INTEGER, "
            + COLUMN_HIGHLIGHTS + " TEXT, "
            + COLUMN_EDITION_NUMBER + " INTEGER, "
            + COLUMN_DESCRIPTION + " TEXT, "
            + COLUMN_SUBSCRIPTION_IDS + " TEXT, "
            + COLUMN_KEYWORDS + " TEXT, "
            + COLUMN_FILE_SIZE + " INTEGER, "
            + COLUMN_TOPIC_IDS + " TEXT, "
            + COLUMN_COVERS + " TEXT, "
            + COLUMN_PUBLISHER + " VARCHAR(50), "
            + COLUMN_COPYRIGHT_YEAR + " INTEGER, "
            + COLUMN_RELEASE_DATE + " VARCHAR(50), "
            + COLUMN_AUTHORS + " TEXT, "
            + COLUMN_DOWNLOAD_PROGRESS_PERCENT + " INTEGER, "
            + COLUMN_FILE_PATH + " TEXT, "
            + COLUMN_IS_FAVORITEN + " VARCHAR(10), "
            + COLUMN_LAST_READ_TIME + " TEXT, "
            + COLUMN_NEED_SYNC_TO_SERVER + " VARCHAR(10), "
            + COLUMN_IS_DOWNLOAD_FAILED + " VARCHAR(10), "
            + COLUMN_NEED_TO_RESUME + " VARCHAR(10), "
            + COLUMN_TIME_STAMP_DOWNLOAD + " TEXT, "
            + COLUMN_X_CONTENT_KEY + " TEXT)";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;


    public static long insertEbook(Ebook ebook) {
        return EbookTable.insertEbook(ebook, TABLE_NAME);
    }

    public static int insertEbookList(List<Ebook> ebookList) {
        return EbookTable.insertEbookList(ebookList, TABLE_NAME);
    }

    public static boolean deleteEbooks() {
        return EbookTable.deleteEbooks(TABLE_NAME);
    }

    public static Ebook getEbook(Integer ebookId) {
        return EbookTable.getEbook(ebookId, TABLE_NAME);
    }

    public static boolean checkEbookDownload(Integer ebookId) {
        return EbookTable.checkEbookDownload(ebookId, TABLE_NAME);
    }

    public static List<Ebook> getEbooks() {
        return EbookTable.getEbooks(TABLE_NAME);
    }

    public static boolean checkEbookIsLoaded() {
        return EbookTable.checkEbookIsLoaded(TABLE_NAME);
    }

    public static Ebook updateEbook(Ebook ebook) {
        return EbookTable.updateEbook(ebook, TABLE_NAME);
    }

    public static void updateEbookFavorite(int id, boolean isFavoriten, boolean needSyncToServer) {
        Ebook ebook = getEbook(id);
        if (ebook != null) {
            ebook.setFavoriten(isFavoriten);
            ebook.setNeedSyncToServer(needSyncToServer);
            EbookTable.update(ebook, TABLE_NAME);
        }
    }

    public static synchronized void updateEbookDownloadProgress(Ebook ebook, int downloadProgressPercent) {
        Ebook ebookFromDatabase = getEbook(ebook.getId());
        if (ebookFromDatabase != null) {
            if (TextUtils.isEmpty(ebookFromDatabase.getContentKey()) || ebookFromDatabase.getDownloadProgress() != downloadProgressPercent) {
                ebookFromDatabase.setDownloadProgress(downloadProgressPercent);
                if (!TextUtils.isEmpty(ebook.getContentKey()))
                    ebookFromDatabase.setContentKey(ebook.getContentKey());
                EbookTable.update(ebookFromDatabase, TABLE_NAME);
            }
        }
    }

    public static List<Ebook> getDownloadedEbooks() {
        return EbookTable.getDownloadedEbooks(TABLE_NAME);
    }

    public static int getDownloadProgressEbook(int ebookId) {
        return EbookTable.getDownloadProgressEbook(ebookId, TABLE_NAME);
    }

    public static int getDownloadedEbooksCount() {
        return EbookTable.getDownloadedEbooksCount(TABLE_NAME);
    }

    public static List<Ebook> getFavoriteNeedSyncEbooks() {
        return EbookTable.getFavoriteNeedSyncEbooks(TABLE_NAME);
    }

    public static boolean deleteEbook(Ebook ebook) {
        return EbookTable.deleteEbook(ebook, TABLE_NAME);
    }

    public static List<Ebook> getFavoritenEbooks() {
        return EbookTable.getFavoritenEbooks(TABLE_NAME);
    }

    public static boolean checkFavoriteIsLoaded() {
        return EbookTable.checkFavoriteIsLoaded(TABLE_NAME);
    }

    public static boolean updateEbookPath(int ebookId, String filePath) {
        Ebook ebook = getEbook(ebookId);
        ebook.setFilePath(filePath);
        return EbookTable.update(ebook, TABLE_NAME);
    }

    public static List<Ebook> getWaitingDownloadBooks() {
        return EbookTable.getWatingDownloadBooks(TABLE_NAME);
    }

    public static boolean checkDownloadFailed(int ebookId) {
        return EbookTable.checkDownloadFailed(ebookId, TABLE_NAME);
    }

    public static List<Ebook> getDownloadingEbooks() {
        return EbookTable.getDownloadingEbooks(TABLE_NAME);
    }


    public static List<Ebook> getNeedDownloadBooks() {
        return EbookTable.getNeedDownloadBooks(TABLE_NAME);
    }

    public static List<Ebook> getAllToResumeFromNetwork() {
        return EbookTable.getAllToResumeFromNetwork(TABLE_NAME);
    }
}

