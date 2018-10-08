package com.sap_press.rheinwerk_reader.download.datamanager.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sap_press.rheinwerk_reader.download.datamanager.tables.EbookTable;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.LastReadTable;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.SubscriptionTable;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.UserTable;

import java.util.ArrayList;
import java.util.List;

public class EbookDbAdapter {

    private Context mContext;
    private static SQLiteDatabase mDatabase;


    public EbookDbAdapter(Context ctx) {
        this.mContext = ctx;
        mDatabase = EbookDbHelper.getInstance(mContext).getMyWritableDatabase();
    }

    public static boolean isDataExits(String query) {
        Cursor c = mDatabase.rawQuery(query, null);
        final boolean isExits = c.getCount() > 0;
        c.close();
        return isExits;
    }

    public static long saveUser(ContentValues userContentValues) {
        return mDatabase.insert(UserTable.USER_TABLE_NAME, null, userContentValues);
    }

    public static Cursor getUser(Long userId) {
        return mDatabase.rawQuery("SELECT * FROM " + UserTable.USER_TABLE_NAME + " WHERE " + UserTable.USER_COLUMN_USER_ID + " = '" + userId + "'", null);
    }

    public static long deleteUser(Long userId) {
        return mDatabase.delete(UserTable.USER_TABLE_NAME, UserTable.USER_COLUMN_USER_ID + " = '" + userId + "'", null);
    }

    public static Cursor getUser() {
        return mDatabase.rawQuery("SELECT * FROM " + UserTable.USER_TABLE_NAME, null);
    }

    public static long saveEbook(ContentValues ebookContentValues, String tableName) {
        return mDatabase.insert(tableName, null, ebookContentValues);
    }

    public static Cursor getEbook(String tableName, Integer ebookId) {
        return mDatabase.rawQuery("SELECT * FROM " + tableName + " WHERE " + EbookTable.COLUMN_ID + " = '" + ebookId + "'", null);
    }

    public static Cursor getEbooks(String tableName) {
        return mDatabase.rawQuery("SELECT * FROM " + tableName, null);
    }

    public static synchronized boolean updateEbook(String tableName, ContentValues ebookContentValues, String id) {
        return mDatabase.update(tableName, ebookContentValues, EbookTable.COLUMN_ID + " = " + id, null) > 0;
    }

    public static long saveSubscription(ContentValues subscriptionContentValues) {
        return mDatabase.insert(SubscriptionTable.SUBSCRIPTION_TABLE_NAME, null, subscriptionContentValues);
    }

    public static Cursor getSubscription(Long subscriptionId) {
        return mDatabase.rawQuery("SELECT * FROM " + SubscriptionTable.SUBSCRIPTION_TABLE_NAME + " WHERE " + SubscriptionTable.SUBSCRIPTION_COLUMN_SUBSCRIPTION_ID + " = '" + subscriptionId + "'", null);
    }

    public static Cursor getListSubscription() {
        return mDatabase.rawQuery("SELECT * FROM " + SubscriptionTable.SUBSCRIPTION_TABLE_NAME, null);
    }

    public static boolean deleteAll(String table) {
        return mDatabase.delete(table, null, null) > 0;
    }

    public static boolean deleteEbook(String table, String id) {
        return mDatabase.delete(table, EbookTable.COLUMN_ID + " = " + id, null) > 0;
    }

    public static boolean deleteAllTable() {
        // query to obtain the names of all tables in your database
        Cursor c = mDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        List<String> tables = new ArrayList<>();

        // iterate over the result set, adding every table name to a list
        while (c.moveToNext()) {
            tables.add(c.getString(0));
        }

        // call DROP TABLE on every table name
        for (String table : tables) {
            String dropQuery = "DELETE FROM " + table;
            mDatabase.execSQL(dropQuery);
        }
        c.close();
        return true;
    }

    public static void updateSubscription(ContentValues subscriptionContentValues, long id) {
        mDatabase.update(SubscriptionTable.SUBSCRIPTION_TABLE_NAME, subscriptionContentValues, SubscriptionTable.SUBSCRIPTION_COLUMN_SUBSCRIPTION_ID + " = " + id, null);
    }

    public static Cursor getExpiredSubscriptions() {
        return mDatabase.rawQuery("SELECT * FROM " + SubscriptionTable.SUBSCRIPTION_TABLE_NAME + " WHERE " + SubscriptionTable.SUBSCRIPTION_COLUMN_SUBSCRIPTION_IS_EXPIRED + " = '" + true + "'", null);
    }

    public static Cursor getSubscriptions() {
        return mDatabase.rawQuery("SELECT * FROM " + SubscriptionTable.SUBSCRIPTION_TABLE_NAME, null);
    }

    public static long deleteSubscription(Long subId) {
        return mDatabase.delete(SubscriptionTable.SUBSCRIPTION_TABLE_NAME, SubscriptionTable.SUBSCRIPTION_COLUMN_SUBSCRIPTION_ID + " = '" + subId + "'", null);
    }

    /*LAST READ TABLE*/
    public static long saveLastRead(ContentValues lastReadContentValues) {
        return mDatabase.insert(LastReadTable.LAST_READ_TABLE_NAME, null, lastReadContentValues);
    }

    public static Cursor getLastRead(int id) {
        return mDatabase.rawQuery("SELECT * FROM " + LastReadTable.LAST_READ_TABLE_NAME + " WHERE " + LastReadTable.LAST_READ_EBOOK_ID + " = " + id, null);
    }

    public static void updateLastRead(ContentValues lastReadContentValues, int id) {
        mDatabase.update(LastReadTable.LAST_READ_TABLE_NAME, lastReadContentValues, LastReadTable.LAST_READ_EBOOK_ID + " = " + id, null);
    }

    public static Cursor getDownloadedEbooks(String tableName) {
        return mDatabase.rawQuery("SELECT * FROM " + tableName + " WHERE " + EbookTable.COLUMN_DOWNLOAD_PROGRESS_PERCENT + " >= " + 0, null);
    }

    public static Cursor getDownloadProgressEbook(int ebookId, String tableName) {
        return mDatabase.rawQuery("SELECT " + EbookTable.COLUMN_DOWNLOAD_PROGRESS_PERCENT + " FROM " + tableName + " WHERE " + EbookTable.COLUMN_ID + " = '" + ebookId + "'", null);
    }

    public static Cursor checkDownloadedEbooks(String tableName, Integer ebookId) {
        return mDatabase.rawQuery("SELECT * FROM " + tableName + " WHERE " + EbookTable.COLUMN_DOWNLOAD_PROGRESS_PERCENT + " >= " + 0 + " and " + EbookTable.COLUMN_ID + " = '" + ebookId + "'", null);
    }

    public static Cursor getFavoriteNeedSyncEbooks(String tableName) {
        return mDatabase.rawQuery("SELECT * FROM " + tableName + " WHERE " + EbookTable.COLUMN_NEED_SYNC_TO_SERVER + " = '" + true + "'", null);
    }

    public static Cursor getFavoritenEbooks(String tableName) {
        return mDatabase.rawQuery("SELECT * FROM " + tableName + " WHERE " + EbookTable.COLUMN_IS_FAVORITEN + " = '" + true + "'", null);
    }

    public static Cursor getWatingDownloadBooks(String tableName) {
        return mDatabase.rawQuery("SELECT * FROM " + tableName + " WHERE " + EbookTable.COLUMN_DOWNLOAD_PROGRESS_PERCENT + " = " + 0, null);
    }

    public static Cursor getDownloadingBooks(String tableName) {
        return mDatabase.rawQuery("SELECT * FROM " + tableName + " WHERE " +
                EbookTable.COLUMN_DOWNLOAD_PROGRESS_PERCENT + " >= " + 0 + " and " +
                EbookTable.COLUMN_DOWNLOAD_PROGRESS_PERCENT + " < " + 100, null);
    }

    public static Cursor getNeedDownloadBooks(String tableName) {
        return mDatabase.rawQuery("SELECT * FROM " + tableName + " WHERE " +
                EbookTable.COLUMN_DOWNLOAD_PROGRESS_PERCENT + " >= " + 0 + " and " +
                EbookTable.COLUMN_DOWNLOAD_PROGRESS_PERCENT + " < " + 100 + " and " +
                EbookTable.COLUMN_IS_DOWNLOAD_FAILED + " = '" + false + "'" + " or " +
                EbookTable.COLUMN_IS_DOWNLOAD_FAILED + " = '" + true + "'" + " and " +
                EbookTable.COLUMN_NEED_TO_RESUME + " = '" + true + "'", null);
    }

    public static Cursor checkDownloadFailed(String tableName, int ebookId) {
        return mDatabase.rawQuery("SELECT * FROM " + tableName + " WHERE " + EbookTable.COLUMN_IS_DOWNLOAD_FAILED + " = '" + true + "' and " + EbookTable.COLUMN_ID + " = '" + ebookId + "'", null);
    }

    public static Cursor getAllToResumeFromNetwork(String tableName) {
        return mDatabase.rawQuery("SELECT * FROM " + tableName + " WHERE " +
                EbookTable.COLUMN_IS_DOWNLOAD_FAILED + " = '" + true + "'" + " and " +
                EbookTable.COLUMN_NEED_TO_RESUME + " = '" + false + "'"+ " or " +
                EbookTable.COLUMN_NEED_TO_RESUME + " = '" + true + "'" , null);
    }
}
