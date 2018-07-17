package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.database.Cursor;

import com.folioreader.model.ReadPosition;
import com.folioreader.model.ReadPositionImpl;

public class ReadPositionTable {

    public static final String TABLE_NAME = "read_position_table";

    public static final String COL_BOOK_ID = "bookId";
    private static final String COL_CHAPTER_ID = "chapterId";
    private static final String COL_CHAPTER_HREF = "chapterHref";
    private static final String COL_CHAPTER_INDEX = "chapterIndex";
    private static final String COL_USING_ID = "usingId";
    private static final String COL_VALUE = "value";


    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( "
            + COL_BOOK_ID + " INTEGER PRIMARY KEY" + ","
            + COL_CHAPTER_ID + " TEXT" + ","
            + COL_CHAPTER_HREF + " TEXT" + ","
            + COL_CHAPTER_INDEX + " INTEGER" + ","
            + COL_USING_ID + " TEXT" + ","
            + COL_VALUE + " TEXT" + ")";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String TAG = ReadPositionTable.class.getSimpleName();

    public static ContentValues getContentValues(ReadPosition readPosition) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BOOK_ID, readPosition.getBookId());
        contentValues.put(COL_CHAPTER_ID, readPosition.getChapterId());
        contentValues.put(COL_CHAPTER_HREF, readPosition.getChapterHref());
        contentValues.put(COL_CHAPTER_INDEX, readPosition.getChapterIndex());
        contentValues.put(COL_USING_ID, String.valueOf(readPosition.isUsingId()));
        contentValues.put(COL_VALUE, readPosition.getValue());
        return contentValues;
    }

    public static ReadPositionImpl getReadPositionById(String bookId) {
        Cursor readPositionCursor = DbAdapter.getReadPosition(bookId);
        ReadPositionImpl readPosition = null;
        while (readPositionCursor.moveToNext()) {
            readPosition = new ReadPositionImpl(
                    readPositionCursor.getString(readPositionCursor.getColumnIndex(COL_BOOK_ID)),
                    readPositionCursor.getString(readPositionCursor.getColumnIndex(COL_CHAPTER_ID)),
                    readPositionCursor.getString(readPositionCursor.getColumnIndex(COL_CHAPTER_HREF)),
                    readPositionCursor.getInt(readPositionCursor.getColumnIndex(COL_CHAPTER_INDEX)),
                    Boolean.parseBoolean(readPositionCursor.getString(readPositionCursor.getColumnIndex(COL_USING_ID))),
                    readPositionCursor.getString(readPositionCursor.getColumnIndex(COL_VALUE)));

        }
        return readPosition;
    }

    private static boolean isExist(ReadPosition readPosition) {
        final Cursor cursor = DbAdapter.getReadPosition(readPosition.getBookId());
        final boolean isExist = cursor.getCount() > 0;
        cursor.close();
        return isExist;
    }

    public static void saveReadPosition(ReadPosition readPosition) {
        if (isExist(readPosition)) {
            updateReadPosition(readPosition);
        } else {
            insertReadPosition(readPosition);
        }
    }


    public static long insertReadPosition(ReadPosition readPosition) {
        return DbAdapter.saveReadPosition(getContentValues(readPosition));
    }

    public static boolean updateReadPosition(ReadPosition readPosition) {
        return DbAdapter.updateReadPosition(getContentValues(readPosition), String.valueOf(readPosition.getBookId()));
    }
}
