package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.folioreader.Constants;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.sap_press.rheinwerk_reader.mod.models.notes.HighlightV2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class HighLightTable {
    public static final String TABLE_NAME = "highlight_table";

    public static final String ID = "_id";
    public static final String COL_BOOK_ID = "bookId";
    private static final String COL_CONTENT = "content";
    private static final String COL_DATE = "date";
    private static final String COL_TYPE = "type";
    private static final String COL_PAGE_NUMBER = "page_number";
    private static final String COL_PAGE_ID = "pageId";
    private static final String COL_RANGY = "rangy";
    private static final String COL_NOTE = "note";
    private static final String COL_UUID = "uuid";
    private static final String COL_SERVER_ID = "serverId";
    private static final String COL_ACCOUNT_ID = "accountId";
    private static final String COL_IS_SYNCED = "isSynced";
    private static final String COL_NUM_OF_TRY = "numOfTry";
    private static final String COL_IS_DELETED = "isDeleted";
    private static final String COL_TITLE = "title";


    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( "
            + ID + " TEXT" + ","
            + COL_BOOK_ID + " TEXT" + ","
            + COL_CONTENT + " TEXT" + ","
            + COL_DATE + " TEXT" + ","
            + COL_TYPE + " TEXT" + ","
            + COL_PAGE_NUMBER + " INTEGER" + ","
            + COL_PAGE_ID + " TEXT" + ","
            + COL_RANGY + " TEXT" + ","
            + COL_UUID + " TEXT" + ","
            + COL_NOTE + " TEXT" + ","
            + COL_SERVER_ID + " INTEGER" + ","
            + COL_ACCOUNT_ID + " INTEGER" + ","
            + COL_IS_SYNCED + " TEXT" + ","
            + COL_NUM_OF_TRY + " INTEGER" + ","
            + COL_IS_DELETED + " TEXT" + ","
            + COL_TITLE + " TEXT" + ")";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String TAG = HighLightTable.class.getSimpleName();

    public static ContentValues getHighlightContentValues(HighLight highLight) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BOOK_ID, highLight.getBookId());
        contentValues.put(COL_CONTENT, highLight.getContent());
        contentValues.put(COL_DATE, getDateTimeString(highLight.getDate()));
        contentValues.put(COL_TYPE, highLight.getType());
        contentValues.put(COL_PAGE_NUMBER, highLight.getPageNumber());
        contentValues.put(COL_PAGE_ID, highLight.getPageId());
        contentValues.put(COL_RANGY, highLight.getRangy());
        contentValues.put(COL_NOTE, highLight.getNote());
        contentValues.put(COL_UUID, highLight.getUUID());
        return contentValues;
    }

    public static ArrayList<HighlightImpl> getAllHighlights(String bookId) {
        ArrayList<HighlightImpl> highlights = new ArrayList<>();
        Cursor highlightCursor = DbAdapter.getHighLightsForBookId(bookId);
        while (highlightCursor.moveToNext()) {
            highlights.add(new HighlightImpl(highlightCursor.getInt(highlightCursor.getColumnIndex(ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_BOOK_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_CONTENT)),
                    getDateTime(highlightCursor.getString(highlightCursor.getColumnIndex(COL_DATE))),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_TYPE)),
                    highlightCursor.getInt(highlightCursor.getColumnIndex(COL_PAGE_NUMBER)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_PAGE_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_RANGY)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_NOTE)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_UUID))));
        }
        return highlights;
    }

    public static HighlightImpl getHighlightId(int id) {
        Cursor highlightCursor = DbAdapter.getHighlightsForId(id);
        HighlightImpl highlightImpl = new HighlightImpl();
        while (highlightCursor.moveToNext()) {
            highlightImpl = new HighlightImpl(highlightCursor.getInt(highlightCursor.getColumnIndex(ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_BOOK_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_CONTENT)),
                    getDateTime(highlightCursor.getString(highlightCursor.getColumnIndex(COL_DATE))),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_TYPE)),
                    highlightCursor.getInt(highlightCursor.getColumnIndex(COL_PAGE_NUMBER)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_PAGE_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_RANGY)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_NOTE)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_UUID)));

        }
        return highlightImpl;
    }

    public static long insertHighlight(HighlightImpl highlightImpl) {
        highlightImpl.setUUID(UUID.randomUUID().toString());
        return DbAdapter.saveHighLight(getHighlightContentValues(highlightImpl));
    }

    public static boolean deleteHighlight(String rangy) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = \"" + rangy + "\"";
        int id = DbAdapter.getIdForQuery(query);
        return id != -1 && deleteHighlight(id);
    }

    public static boolean deleteHighlight(int highlightId) {
        return DbAdapter.deleteById(TABLE_NAME, ID, String.valueOf(highlightId));
    }

    public static List<String> getHighlightsForPageId(String pageId) {
        String query = "SELECT " + COL_RANGY + " FROM " + TABLE_NAME + " WHERE " + COL_PAGE_ID + " = \"" + pageId + "\"";
        Cursor c = DbAdapter.getHighlightsForPageId(query, pageId);
        List<String> rangyList = new ArrayList<>();
        while (c.moveToNext()) {
            rangyList.add(c.getString(c.getColumnIndex(COL_RANGY)));
        }
        c.close();
        return rangyList;
    }

    public static boolean updateHighlight(HighlightImpl highlightImpl) {
        return DbAdapter.updateHighLight(getHighlightContentValues(highlightImpl), String.valueOf(highlightImpl.getId()));
    }

    public static String getDateTimeString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static Date getDateTime(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT, Locale.getDefault());
        Date date1 = new Date();
        try {
            date1 = dateFormat.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, "Date parsing failed", e);
        }
        return date1;
    }

    public static HighlightImpl updateHighlightStyle(String rangy, String style) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = \"" + rangy + "\"";
        int id = DbAdapter.getIdForQuery(query);
        if (id != -1 && update(id, updateRangy(rangy, style), style.replace("highlight_", ""))) {
            return getHighlightId(id);
        }
        return null;
    }

    public static HighlightImpl getHighlightForRangy(String rangy) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = \"" + rangy + "\"";
        return getHighlightId(DbAdapter.getIdForQuery(query));
    }

    private static String updateRangy(String rangy, String style) {
        /*Pattern p = Pattern.compile("\\highlight_\\w+");
        Matcher m = p.matcher(rangy);
        return m.replaceAll(style);*/
        String[] s = rangy.split("\\$");
        StringBuilder builder = new StringBuilder();
        for (String p : s) {
            if (TextUtils.isDigitsOnly(p)) {
                builder.append(p);
                builder.append('$');
            } else {
                builder.append(style);
                builder.append('$');
            }
        }
        return builder.toString();
    }

    private static boolean update(int id, String s, String color) {
        HighlightImpl highlightImpl = getHighlightId(id);
        highlightImpl.setRangy(s);
        highlightImpl.setType(color);
        return DbAdapter.updateHighLight(getHighlightContentValues(highlightImpl), String.valueOf(id));
    }

    public static void saveHighlightIfNotExists(HighLight highLight) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_UUID + " = \"" + highLight.getUUID() + "\"";
        int id = DbAdapter.getIdForQuery(query);
        if (id == -1) {
            DbAdapter.saveHighLight(getHighlightContentValues(highLight));
        }
    }

    /**
     * ###########################################################################################
     * This is Highlight for version 2.0
     */
    public static ContentValues getHighlightItemContentValues(HighlightV2 highLight) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BOOK_ID, String.valueOf(highLight.getProductId()));
        contentValues.put(COL_CONTENT, highLight.getMarkedText());
        contentValues.put(COL_DATE, highLight.getCreatedAt());
        contentValues.put(COL_TYPE, highLight.getType());
        contentValues.put(COL_PAGE_NUMBER, highLight.getPageIndex());
        contentValues.put(COL_PAGE_ID, highLight.getFilePath());
        contentValues.put(COL_RANGY, highLight.getRange());
        contentValues.put(COL_NOTE, highLight.getNote());
        contentValues.put(COL_UUID, highLight.getUUID());
        contentValues.put(COL_SERVER_ID, highLight.getId());
        contentValues.put(COL_ACCOUNT_ID, highLight.getAccountId());
        contentValues.put(COL_IS_SYNCED, String.valueOf(highLight.isSynced()));
        contentValues.put(COL_NUM_OF_TRY, highLight.getNumOfTry());
        contentValues.put(COL_IS_DELETED, String.valueOf(highLight.isDeleted()));
        contentValues.put(COL_TITLE, highLight.getTitle());
        return contentValues;
    }

    public static ArrayList<HighlightV2> getAllHighlightItems(String bookId) {
        ArrayList<HighlightV2> highlights = new ArrayList<>();
        Cursor highlightCursor = DbAdapter.getHighLightsForBookId(bookId);
        while (highlightCursor.moveToNext()) {
            highlights.add(getHighlightItemFromCursor(highlightCursor));
        }
        return highlights;
    }

    public static HighlightV2 getHighlightItemById(String id) {
        Cursor highlightCursor = DbAdapter.getHighlightItemsForId(id);
        HighlightV2 highlight = new HighlightV2();
        while (highlightCursor.moveToNext()) {
            highlight = getHighlightItemFromCursor(highlightCursor);
        }
        return highlight;
    }

    @NonNull
    private static HighlightV2 getHighlightItemFromCursor(Cursor cursor) {
        HighlightV2 highlight = new HighlightV2();
        highlight.setId(cursor.getInt(cursor.getColumnIndex(COL_SERVER_ID)));
        highlight.setAccountId(cursor.getInt(cursor.getColumnIndex(COL_ACCOUNT_ID)));
        highlight.setCreatedAt(cursor.getString(cursor.getColumnIndex(COL_DATE)));
        highlight.setFilePath(cursor.getString(cursor.getColumnIndex(COL_PAGE_ID)));
        highlight.setMarkedText(cursor.getString(cursor.getColumnIndex(COL_CONTENT)));
        highlight.setNote(cursor.getString(cursor.getColumnIndex(COL_NOTE)));
        highlight.setPageIndex(cursor.getInt(cursor.getColumnIndex(COL_PAGE_NUMBER)));
        highlight.setProductId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COL_BOOK_ID))));
        highlight.setRange(cursor.getString(cursor.getColumnIndex(COL_RANGY)));
        highlight.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
        highlight.setType(cursor.getString(cursor.getColumnIndex(COL_TYPE)));
        highlight.setUuid(cursor.getString(cursor.getColumnIndex(COL_UUID)));
        highlight.setDeleted(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(COL_IS_DELETED))));
        highlight.setSynced(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(COL_IS_SYNCED))));
        highlight.setNumOfTry(cursor.getInt(cursor.getColumnIndex(COL_NUM_OF_TRY)));
        highlight.setId(cursor.getInt(cursor.getColumnIndex(COL_SERVER_ID)));
        return highlight;
    }

    public static long insertHighlightItem(HighlightV2 highlight) {
        highlight.setUuid(UUID.randomUUID().toString());
        return DbAdapter.saveHighLight(getHighlightItemContentValues(highlight));
    }

    public static boolean deleteHighlightItem(String highlightId) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + ID + " = \"" + highlightId + "\"";
        int id = DbAdapter.getIdForQuery(query);
        return id != -1 && deleteHighlight(id);
    }

    public static List<String> getHighlightItemsForPageId(String pageId) {
        String query = "SELECT " + COL_RANGY + " FROM " + TABLE_NAME + " WHERE " + COL_PAGE_ID + " = \"" + pageId + "\"";
        Cursor c = DbAdapter.getHighlightsForPageId(query, pageId);
        List<String> rangyList = new ArrayList<>();
        while (c.moveToNext()) {
            rangyList.add(c.getString(c.getColumnIndex(COL_RANGY)));
        }
        c.close();
        return rangyList;
    }

    public static boolean updateHighlight(HighlightV2 highlightV2) {
        return DbAdapter.updateHighLight(getHighlightItemContentValues(highlightV2), String.valueOf(highlightV2.getId()));
    }

    public static HighlightV2 getHighlightItemForRangy(String rangy) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = \"" + rangy + "\"";
        return getHighlightItemById(DbAdapter.getHighlightIdForQuery(query));
    }
}



