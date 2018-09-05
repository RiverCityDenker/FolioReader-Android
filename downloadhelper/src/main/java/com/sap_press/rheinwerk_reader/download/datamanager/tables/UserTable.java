package com.sap_press.rheinwerk_reader.download.datamanager.tables;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;

import com.sap_press.rheinwerk_reader.download.datamanager.sqlite.EbookDbAdapter;
import com.sap_press.rheinwerk_reader.download.models.user.User;

public class UserTable {
    public static final String USER_TABLE_NAME = "users";


    public static final String USER_COLUMN_USER_ID = "id";
    private static final String USER_COLUMN_USER_FIRST_NAME = "first_name";
    private static final String USER_COLUMN_USER_LAST_NAME = "last_name";
    private static final String USER_COLUMN_USER_STATUS = "status";
    private static final String USER_COLUMN_USER_LINKS = "_links";
    private static final String USER_COLUMN_USER_EMAIL = "email";

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS "
            + USER_TABLE_NAME + "("
            + USER_COLUMN_USER_ID + " LONG PRIMARY KEY, "
            + USER_COLUMN_USER_FIRST_NAME + " VARCHAR(20), "
            + USER_COLUMN_USER_LAST_NAME + " VARCHAR(50), "
            + USER_COLUMN_USER_STATUS + " VARCHAR(10), "
            + USER_COLUMN_USER_LINKS + " TEXT, "
            + USER_COLUMN_USER_EMAIL + " VARCHAR(50))";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + USER_TABLE_NAME;

    private static ContentValues getUserContentValues(User user) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_COLUMN_USER_FIRST_NAME, user.getFirstName());
        contentValues.put(USER_COLUMN_USER_LAST_NAME, user.getLastName());
        contentValues.put(USER_COLUMN_USER_ID, user.getCustomerId());
        contentValues.put(USER_COLUMN_USER_STATUS, String.valueOf(user.isIsActive()));
        contentValues.put(USER_COLUMN_USER_EMAIL, user.getEmail());
        contentValues.put(USER_COLUMN_USER_LINKS, user.getLinkString());
        return contentValues;
    }

    public static long insertUser(User user) {
        return EbookDbAdapter.saveUser(getUserContentValues(user));
    }

    public static User getUser(Long userId) {
        Cursor cursor = EbookDbAdapter.getUser(userId);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            User user = new User();
            user.setCustomerId(cursor.getLong(cursor.getColumnIndex(USER_COLUMN_USER_ID)));
            user.setFirstName(cursor.getString(cursor.getColumnIndex(USER_COLUMN_USER_FIRST_NAME)));
            user.setLastName(cursor.getString(cursor.getColumnIndex(USER_COLUMN_USER_LAST_NAME)));
            user.setIsActive(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(USER_COLUMN_USER_STATUS))));
            user.setEmail(cursor.getString(cursor.getColumnIndex(USER_COLUMN_USER_EMAIL)));
            user.setLinks(cursor.getString(cursor.getColumnIndex(USER_COLUMN_USER_LINKS)));
            cursor.close();
            return user;
        } else {
            cursor.close();
            throw new Resources.NotFoundException("User with id " + userId + " does not exists");
        }
    }

    public static boolean checkUserIsExits(Long userId) {
        boolean isExits;
        Cursor cursor = EbookDbAdapter.getUser(userId);
        isExits = cursor.getCount() > 0;
        return isExits;
    }

    public static long deleteUser(Long userId) {
        return EbookDbAdapter.deleteUser(userId);
    }

    public static User getUser() {
        Cursor cursor = EbookDbAdapter.getUser();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            User user = new User();
            user.setCustomerId(cursor.getLong(cursor.getColumnIndex(USER_COLUMN_USER_ID)));
            user.setFirstName(cursor.getString(cursor.getColumnIndex(USER_COLUMN_USER_FIRST_NAME)));
            user.setLastName(cursor.getString(cursor.getColumnIndex(USER_COLUMN_USER_LAST_NAME)));
            user.setIsActive(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(USER_COLUMN_USER_STATUS))));
            user.setEmail(cursor.getString(cursor.getColumnIndex(USER_COLUMN_USER_EMAIL)));
            user.setLinks(cursor.getString(cursor.getColumnIndex(USER_COLUMN_USER_LINKS)));
            cursor.close();
            return user;
        } else {
            cursor.close();
            return null;
        }
    }
}
