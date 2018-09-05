package com.sap_press.rheinwerk_reader.download.datamanager.tables;

import android.content.ContentValues;
import android.database.Cursor;

import com.sap_press.rheinwerk_reader.download.datamanager.sqlite.EbookDbAdapter;
import com.sap_press.rheinwerk_reader.download.models.ebooks.Ebook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by hale on 6/11/2018.
 */
public class LastReadTable {
    public static final String LAST_READ_TABLE_NAME = "last_read";


    public static final String LAST_READ_EBOOK_ID = "id";
    private static final String LAST_READ_EBOOK_TITLE = "title";
    private static final String LAST_READ_EBOOK_LAST_READ = "last_read";

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS "
            + LAST_READ_TABLE_NAME + "("
            + LAST_READ_EBOOK_ID + " LONG PRIMARY KEY, "
            + LAST_READ_EBOOK_TITLE + " VARCHAR(100), "
            + LAST_READ_EBOOK_LAST_READ + " integer not null)";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + LAST_READ_TABLE_NAME;

    private static ContentValues getLastReadContentValues(long id, String title, long timestamp) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LAST_READ_EBOOK_ID, id);
        contentValues.put(LAST_READ_EBOOK_TITLE, title);
        contentValues.put(LAST_READ_EBOOK_LAST_READ, timestamp);
        return contentValues;
    }

    public static void insertLastRead(int id, String title, long timestamp) {
        if (checkLastReadIsExits(id)) {
            EbookDbAdapter.updateLastRead(getLastReadContentValues(id, title, timestamp), id);
        } else {
            EbookDbAdapter.saveLastRead(getLastReadContentValues(id, title, timestamp));
        }
    }

    public static boolean checkLastReadIsExits(int id) {
        boolean isExits;
        Cursor cursor = EbookDbAdapter.getLastRead(id);
        isExits = cursor.getCount() > 0;
        cursor.close();
        return isExits;
    }

    public static List<Ebook> sortEbooksByLastRead(List<Ebook> ebooks) {
        List<Ebook> ebookList = new ArrayList<>();
        List<CustomeEbook> customeEbookList = new ArrayList<>();
        for (int i = 0; i < ebooks.size(); i++) {
            Ebook ebook = ebooks.get(i);
            Cursor cursor = EbookDbAdapter.getLastRead(ebook.getId());
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(LAST_READ_EBOOK_LAST_READ));
                customeEbookList.add(new CustomeEbook(timestamp, ebook));
            } else {
                customeEbookList.add(new CustomeEbook(0, ebook));
            }
            cursor.close();
        }

        Collections.sort(customeEbookList, new Comparator<CustomeEbook>() {
            @Override
            public int compare(CustomeEbook customeEbook1, CustomeEbook customeEbook2) {
                return Long.compare(customeEbook2.getTimestamp(), customeEbook1.getTimestamp());
            }
        });

        for (CustomeEbook ebook : customeEbookList) {
            ebookList.add(ebook.getEbook());
        }

        return ebookList;
    }

    public static class CustomeEbook {
        private long timestamp;
        private Ebook ebook;

        public CustomeEbook(long timestamp, Ebook ebook) {
            this.timestamp = timestamp;
            this.ebook = ebook;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public Ebook getEbook() {
            return ebook;
        }

        public void setEbook(Ebook ebook) {
            this.ebook = ebook;
        }
    }

}
