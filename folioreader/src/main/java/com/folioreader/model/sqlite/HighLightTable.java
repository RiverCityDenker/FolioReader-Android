package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.folioreader.Constants;
import com.sap_press.rheinwerk_reader.mod.models.highlight.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class HighLightTable {
    public static final String TABLE_NAME = "highlight_table";

    public static final String COL_INTERNAL_ID = "internalId";
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
            + COL_INTERNAL_ID + " TEXT" + ","
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

    public static String getDateTimeString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * ###########################################################################################
     * This is Highlight for version 2.0
     */
    public static ContentValues getHighlightItemContentValues(Note note) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_INTERNAL_ID, note.getInternalId());
        contentValues.put(COL_BOOK_ID, String.valueOf(note.getProductId()));
        contentValues.put(COL_CONTENT, note.getMarkedText());
        contentValues.put(COL_DATE, note.getCreatedAt());
        contentValues.put(COL_TYPE, note.getType());
        contentValues.put(COL_PAGE_NUMBER, note.getPageIndex());
        contentValues.put(COL_PAGE_ID, note.getFilePath());
        contentValues.put(COL_RANGY, note.getRange());
        contentValues.put(COL_NOTE, note.getNoteText());
        contentValues.put(COL_UUID, note.getUUID());
        contentValues.put(COL_SERVER_ID, note.getId());
        contentValues.put(COL_ACCOUNT_ID, note.getAccountId());
        contentValues.put(COL_IS_SYNCED, String.valueOf(note.isSynced()));
        contentValues.put(COL_NUM_OF_TRY, note.getNumOfTry());
        contentValues.put(COL_IS_DELETED, String.valueOf(note.isDeleted()));
        contentValues.put(COL_TITLE, note.getTitle());
        return contentValues;
    }

    public static ArrayList<Note> getAllHighlightItems(String bookId) {
        ArrayList<Note> highlights = new ArrayList<>();
        Cursor highlightCursor = DbAdapter.getHighLightsForBookId(bookId);
        while (highlightCursor.moveToNext()) {
            highlights.add(getHighlightItemFromCursor(highlightCursor));
        }
        return highlights;
    }

    public static Note getHighlightItemById(String id) {
        Cursor highlightCursor = DbAdapter.getHighlightItemsForId(id);
        Note highlight = new Note();
        while (highlightCursor.moveToNext()) {
            highlight = getHighlightItemFromCursor(highlightCursor);
        }
        return highlight;
    }

    @NonNull
    private static Note getHighlightItemFromCursor(Cursor cursor) {
        Note highlight = new Note();
        highlight.setId(cursor.getInt(cursor.getColumnIndex(COL_SERVER_ID)));
        highlight.setAccountId(cursor.getInt(cursor.getColumnIndex(COL_ACCOUNT_ID)));
        highlight.setCreatedAt(cursor.getString(cursor.getColumnIndex(COL_DATE)));
        highlight.setFilePath(cursor.getString(cursor.getColumnIndex(COL_PAGE_ID)));
        highlight.setMarkedText(cursor.getString(cursor.getColumnIndex(COL_CONTENT)));
        highlight.setNoteText(cursor.getString(cursor.getColumnIndex(COL_NOTE)));
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

    public static long insertHighlightItem(Note highlight) {
        highlight.setUuid(UUID.randomUUID().toString());
        return DbAdapter.saveHighLight(getHighlightItemContentValues(highlight));
    }

    public static boolean deleteHighlightItem(String highlightId) {
        String query = "SELECT " + COL_INTERNAL_ID + " FROM " + TABLE_NAME + " WHERE " + COL_INTERNAL_ID + " = \"" + highlightId + "\"";
        String internalId = DbAdapter.getHighlightIdForQuery(query);
        return !TextUtils.isEmpty(internalId) && deleteHighlight(internalId);
    }

    public static boolean deleteHighlight(String internalId) {
        return DbAdapter.deleteById(TABLE_NAME, COL_INTERNAL_ID, internalId);
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

    public static boolean updateHighlight(Note note) {
        return DbAdapter.updateHighLight(getHighlightItemContentValues(note), String.valueOf(note.getInternalId()));
    }

    public static Note getHighlightItemForRangy(String rangy) {
        String query = "SELECT " + COL_INTERNAL_ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = \"" + rangy + "\"";
        return getHighlightItemById(DbAdapter.getHighlightIdForQuery(query));
    }
}



