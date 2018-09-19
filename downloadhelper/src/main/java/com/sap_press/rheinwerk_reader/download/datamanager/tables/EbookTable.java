package com.sap_press.rheinwerk_reader.download.datamanager.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sap_press.rheinwerk_reader.download.datamanager.sqlite.EbookDbAdapter;
import com.sap_press.rheinwerk_reader.mod.models.ebooks.Ebook;

import java.util.ArrayList;
import java.util.List;

public class EbookTable {

    private static final String TAG = EbookTable.class.getSimpleName();
    public static String TABLE_NAME = "ebooks";

    public static String COLUMN_ID = "id";
    public final static String COLUMN_TITLE = "title";
    public final static String COLUMN_SUBTITLE = "subtitle";
    public final static String COLUMN_LAST_READ = "last_read";
    public final static String COLUMN_ESBN = "isbn";
    public final static String COLUMN_IS_STANDALONE = "is_standalone";
    public final static String COLUMN_CLAIM = "claim";
    public final static String COLUMN_EDITION_TEXT = "edition_text";
    public final static String COLUMN_PAGE_NUMBER = "page_number";
    public final static String COLUMN_HIGHLIGHTS = "highlights";
    public final static String COLUMN_EDITION_NUMBER = "edition_number";
    public final static String COLUMN_DESCRIPTION = "description";
    public final static String COLUMN_SUBSCRIPTION_IDS = "subscription_ids";
    public final static String COLUMN_KEYWORDS = "keywords";
    public final static String COLUMN_FILE_SIZE = "file_size";
    public final static String COLUMN_TOPIC_IDS = "topic_ids";
    public final static String COLUMN_COVERS = "covers";
    public final static String COLUMN_PUBLISHER = "publisher";
    public final static String COLUMN_COPYRIGHT_YEAR = "copyright_year";
    public final static String COLUMN_RELEASE_DATE = "release_date";
    public final static String COLUMN_AUTHORS = "authors";
    public final static String COLUMN_LINKS = "_links";
    public static String COLUMN_DOWNLOAD_PROGRESS_PERCENT = "download_progress_percent";
    public static String COLUMN_FILE_PATH = "file_path";
    public static String COLUMN_X_CONTENT_KEY = "x_content_path";
    public static String COLUMN_IS_FAVORITEN = "is_favoriten";
    public static String COLUMN_LAST_READ_TIME = "last_read_time";
    public static String COLUMN_NEED_SYNC_TO_SERVER = "need_sync_to_server";
    public static String COLUMN_TIME_STAMP_DOWNLOAD = "timestamp_download_start";


    private static boolean isTableHasRecord(String tableName) {
        final Cursor cursor = EbookDbAdapter.getEbooks(tableName);
        final boolean isExist = cursor.getCount() > 0;
        cursor.close();
        return isExist;
    }

    private static boolean isEbookExist(int ebookId, String tableName) {
        final Cursor cursor = EbookDbAdapter.getEbook(tableName, ebookId);
        final boolean isExist = cursor.getCount() > 0;
        cursor.close();
        return isExist;
    }

    private static ContentValues getEbookContentValues(Ebook ebook) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, ebook.getId());
        contentValues.put(COLUMN_TITLE, ebook.getTitle());
        contentValues.put(COLUMN_SUBTITLE, ebook.getSubtitle());
        contentValues.put(COLUMN_LAST_READ, ebook.getLastReadString());
        contentValues.put(COLUMN_LINKS, ebook.getLinksString());
        contentValues.put(COLUMN_ESBN, ebook.getIsbn());
        contentValues.put(COLUMN_IS_STANDALONE, String.valueOf(ebook.getStandalone()));
        contentValues.put(COLUMN_CLAIM, ebook.getClaim());
        contentValues.put(COLUMN_EDITION_TEXT, ebook.getEditionText());
        contentValues.put(COLUMN_PAGE_NUMBER, ebook.getPageNumber());
        contentValues.put(COLUMN_HIGHLIGHTS, ebook.getHighlightsString());
        contentValues.put(COLUMN_EDITION_NUMBER, ebook.getEditionNumber());
        contentValues.put(COLUMN_DESCRIPTION, ebook.getDescription());
        contentValues.put(COLUMN_SUBSCRIPTION_IDS, ebook.getSubscriptionIdsString());
        contentValues.put(COLUMN_KEYWORDS, ebook.getKeywordsString());
        contentValues.put(COLUMN_FILE_SIZE, ebook.getFileSize());
        contentValues.put(COLUMN_TOPIC_IDS, ebook.getTopicIdsString());
        contentValues.put(COLUMN_COVERS, ebook.getCoversString());
        contentValues.put(COLUMN_PUBLISHER, ebook.getPublisher());
        contentValues.put(COLUMN_COPYRIGHT_YEAR, ebook.getCopyrightYear());
        contentValues.put(COLUMN_RELEASE_DATE, ebook.getReleaseDate());
        contentValues.put(COLUMN_AUTHORS, ebook.getAuthorsString());
        contentValues.put(COLUMN_DOWNLOAD_PROGRESS_PERCENT, ebook.getDownloadProgress());
        contentValues.put(COLUMN_FILE_PATH, ebook.getFilePath());
        contentValues.put(COLUMN_X_CONTENT_KEY, ebook.getContentKey());
        contentValues.put(COLUMN_IS_FAVORITEN, String.valueOf(ebook.isFavoriten()));
        contentValues.put(COLUMN_NEED_SYNC_TO_SERVER, String.valueOf(ebook.isNeedSyncToServer()));
        contentValues.put(COLUMN_LAST_READ_TIME, ebook.getLastReadTime());
        contentValues.put(COLUMN_TIME_STAMP_DOWNLOAD, String.valueOf(ebook.getDownloadTimeStamp()));
        return contentValues;
    }

    public static long insertEbook(Ebook ebook, String tableName) {
        if (isEbookExist(ebook.getId(), tableName)) return -1;
        return EbookDbAdapter.saveEbook(getEbookContentValues(ebook), tableName);
    }

    public static int insertEbookList(List<Ebook> ebookList, String tableName) {
        int i = 0;
        if (ebookList != null) {
            for (i = 0; i < ebookList.size(); i++) {
                final Ebook ebook = ebookList.get(i);
                if (!isEbookExist(ebook.getId(), tableName)) {
                    EbookDbAdapter.saveEbook(getEbookContentValues(ebook), tableName);
                } else {
                    Log.e(TAG, "insertEbookList: >>>" + ebook.getId());
                    final Ebook ebookFromLocal = getEbook(ebook.getId(), tableName);
                    final int downloadProgress = ebookFromLocal.getDownloadProgress();
                    /*Reset favotite ebook*/
                    ebookFromLocal.setFavoriten(false);
                    //ebookFromLocal.resetDownloadProgress(downloadProgress > 0 && downloadProgress < 100);
                    updateLocalBook(ebook, ebookFromLocal, tableName);
                }
            }
        }
        return i;
    }

    private static void updateLocalBook(Ebook ebook, Ebook ebookFromLocal, String tableName) {
        ebook.setDownloadProgress(ebookFromLocal.getDownloadProgress());
        ebook.setFavoriten(ebookFromLocal.isFavoriten());
        ebook.setFilePath(ebookFromLocal.getFilePath());
        ebook.setContentKey(ebookFromLocal.getContentKey());
        ebook.setLastReadTime(ebookFromLocal.getLastReadTime());
        Log.e(TAG, "updateLocalBook: >>>" + ebook.getDownloadProgress());
        update(ebook, tableName);
    }

    public static int insertDownloadEbookList(List<Ebook> ebookList, String tableName) {
        int i;
        for (i = 0; i < ebookList.size(); i++) {
            final Ebook ebook = ebookList.get(i);
            if (!isEbookExist(ebook.getId(), tableName)) {
                EbookDbAdapter.saveEbook(getEbookContentValues(ebook), tableName);
            }
        }
        return i;
    }

    public static boolean deleteEbooks(String tableName) {
        return EbookDbAdapter.deleteAll(tableName);
    }

    public static Ebook getEbook(Integer ebookId, String tableName) {
        Ebook ebook = null;
        Cursor cursor = EbookDbAdapter.getEbook(tableName, ebookId);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            ebook = getEbookFromCursor(cursor);
            cursor.close();
        } else {
            cursor.close();
            Log.e(TAG, "getEbook: >>>" + "Ebook with id " + ebookId + " does not exists");
        }
        return ebook;
    }

    public static boolean checkEbookDownload(Integer ebookId, String tableName) {
        boolean isExis;
        Cursor cursor = EbookDbAdapter.checkDownloadedEbooks(tableName, ebookId);
        isExis = cursor.getCount() > 0;
        cursor.close();
        return isExis;
    }

    @NonNull
    private static Ebook getEbookFromCursor(Cursor cursor) {
        Ebook ebook = new Ebook();
        ebook.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
        ebook.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
        ebook.setSubtitle(cursor.getString(cursor.getColumnIndex(COLUMN_SUBTITLE)));
        ebook.setLastRead(cursor.getString(cursor.getColumnIndex(COLUMN_LAST_READ)));
        ebook.setIsbn(cursor.getString(cursor.getColumnIndex(COLUMN_ESBN)));
        ebook.setIsStandalone(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(COLUMN_IS_STANDALONE))));
        ebook.setClaim(cursor.getString(cursor.getColumnIndex(COLUMN_CLAIM)));
        ebook.setEditionText(cursor.getString(cursor.getColumnIndex(COLUMN_EDITION_TEXT)));
        ebook.setPageNumber(cursor.getInt(cursor.getColumnIndex(COLUMN_PAGE_NUMBER)));
        ebook.setHighlightString(cursor.getString(cursor.getColumnIndex(COLUMN_HIGHLIGHTS)));
        ebook.setEditionNumber(cursor.getInt(cursor.getColumnIndex(COLUMN_EDITION_NUMBER)));
        ebook.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
        ebook.setSubscriptionIdsString(cursor.getString(cursor.getColumnIndex(COLUMN_SUBSCRIPTION_IDS)));
        ebook.setKeywordsString(cursor.getString(cursor.getColumnIndex(COLUMN_KEYWORDS)));
        ebook.setFileSize(cursor.getInt(cursor.getColumnIndex(COLUMN_FILE_SIZE)));
        ebook.setTopicIdsString(cursor.getString(cursor.getColumnIndex(COLUMN_TOPIC_IDS)));
        ebook.setCoversString(cursor.getString(cursor.getColumnIndex(COLUMN_COVERS)));
        ebook.setPublisher(cursor.getString(cursor.getColumnIndex(COLUMN_PUBLISHER)));
        ebook.setCopyrightYear(cursor.getInt(cursor.getColumnIndex(COLUMN_COPYRIGHT_YEAR)));
        ebook.setReleaseDate(cursor.getString(cursor.getColumnIndex(COLUMN_RELEASE_DATE)));
        ebook.setAuthorsString(cursor.getString(cursor.getColumnIndex(COLUMN_AUTHORS)));
        ebook.setDownloadProgress(cursor.getInt(cursor.getColumnIndex(COLUMN_DOWNLOAD_PROGRESS_PERCENT)));
        ebook.setFilePath(cursor.getString(cursor.getColumnIndex(COLUMN_FILE_PATH)));
        ebook.setContentKey(cursor.getString(cursor.getColumnIndex(COLUMN_X_CONTENT_KEY)));
        ebook.setFavoriten(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(COLUMN_IS_FAVORITEN))));
        ebook.setNeedSyncToServer(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(COLUMN_NEED_SYNC_TO_SERVER))));
        ebook.setDownloadTimeStamp(Long.parseLong(cursor.getString(cursor.getColumnIndex(COLUMN_TIME_STAMP_DOWNLOAD))));
        ebook.setLastReadTime(cursor.getString(cursor.getColumnIndex(COLUMN_LAST_READ_TIME)));
        ebook.setLinksString(cursor.getString(cursor.getColumnIndex(COLUMN_LINKS)));
        return ebook;
    }

    public static List<Ebook> getEbooks(String tableName) {
        Cursor cursor = EbookDbAdapter.getEbooks(tableName);
        return getEbooksFromQuery(cursor);
    }

    public static boolean checkEbookIsLoaded(String tableName) {
        boolean isLoaded;
        Cursor cursor = EbookDbAdapter.getEbooks(tableName);
        isLoaded = cursor.getCount() > 0;
        cursor.close();
        return isLoaded;
    }

    public static List<Ebook> getEbooksByListId(List<Long> listId) {
        List<Ebook> ebookList = getEbooks(TABLE_NAME);
        List<Ebook> result = new ArrayList<>();
        for (int i = 0; i < listId.size(); i++) {
            long id = listId.get(i);
            for (int j = 0; j < ebookList.size(); j++) {
                if (id == ebookList.get(j).getId()) {
                    result.add(ebookList.get(j));
                    break;
                }
            }
        }
        return result;
    }

    public static synchronized boolean update(Ebook ebook, String tableName) {
        Log.e(TAG, "update: >>>" + ebook.getId() + " --- " + ebook.getDownloadProgress() + "%" + " === contentKey = " + ebook.getContentKey());
        return EbookDbAdapter.updateEbook(tableName, getEbookContentValues(ebook), String.valueOf(ebook.getId()));
    }

    public static synchronized Ebook updateEbook(Ebook ebook, String tableName) {
        final int ebookId = ebook.getId();
        if (isEbookExist(ebookId, tableName)) {
            if (ebookId != -1 && update(ebook, tableName)) {
                return getEbook(ebookId, tableName);
            }
        } else {
            insertEbook(ebook, tableName);
            Log.e(TAG, "insertEbook: >>>" + ebook.getDownloadProgress());
            return ebook;
        }
        return ebook;
    }

    public static boolean deleteEbook(Ebook ebook, String tableName) {
        return EbookDbAdapter.deleteEbook(tableName, String.valueOf(ebook.getId()));
    }

    public static List<Ebook> getDownloadedEbooks(String tableName) {
        Cursor cursor = EbookDbAdapter.getDownloadedEbooks(tableName);
        return getEbooksFromQuery(cursor);
    }

    public static int getDownloadProgressEbook(int ebookId, String tableName) {
        Cursor cursor = EbookDbAdapter.getDownloadProgressEbook(ebookId, tableName);
        int progress = -1;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            progress = cursor.getInt(cursor.getColumnIndex(COLUMN_DOWNLOAD_PROGRESS_PERCENT));
        }
        cursor.close();
        return progress;
    }

    public static int getDownloadedEbooksCount(String tableName) {
        int count;
        Cursor cursor = EbookDbAdapter.getDownloadedEbooks(tableName);
        count = cursor.getCount();
        return count;
    }

    @NonNull
    private static List<Ebook> getEbooksFromQuery(Cursor cursor) {
        if (cursor.getCount() > 0) {
            List<Ebook> ebookList = new ArrayList<>();
            while (cursor.moveToNext()) {
                ebookList.add(getEbookFromCursor(cursor));
            }
            cursor.close();
            return ebookList;
        } else {
            cursor.close();
            return new ArrayList<>();
        }
    }

    public static List<Ebook> getFavoriteNeedSyncEbooks(String tableName) {
        Cursor cursor = EbookDbAdapter.getFavoriteNeedSyncEbooks(tableName);
        return getEbooksFromQuery(cursor);
    }

    public static List<Ebook> getFavoritenEbooks(String tableName) {
        Cursor cursor = EbookDbAdapter.getFavoritenEbooks(tableName);
        return getEbooksFromQuery(cursor);
    }

    public static boolean checkFavoriteIsLoaded(String tableName) {
        boolean isLoaded;
        Cursor cursor = EbookDbAdapter.getFavoritenEbooks(tableName);
        isLoaded = cursor.getCount() > 0;
        cursor.close();
        return isLoaded;
    }

    public static List<Ebook> getWatingDownloadBooks(String tableName) {
        Cursor cursor = EbookDbAdapter.getWatingDownloadBooks(tableName);
        return getEbooksFromQuery(cursor);
    }

    public static List<Ebook> getDownloadingEbooks(String tableName) {
        Cursor cursor = EbookDbAdapter.getDownloadingBooks(tableName);
        return getEbooksFromQuery(cursor);
    }

    public static void resetDownloadStateBook(String ebookId, String tableName) {
        final Ebook ebookFromLocal = getEbook(Integer.parseInt(ebookId), tableName);
        ebookFromLocal.resetInfo();
        update(ebookFromLocal, tableName);
    }
}

