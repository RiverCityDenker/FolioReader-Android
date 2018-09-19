package com.sap_press.rheinwerk_reader.download.datamanager.tables;

import android.content.ContentValues;
import android.database.Cursor;

import com.sap_press.rheinwerk_reader.download.datamanager.sqlite.EbookDbAdapter;
import com.sap_press.rheinwerk_reader.mod.models.Subscription;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hale on 5/3/2018.
 */

public class SubscriptionTable {
    public static final String SUBSCRIPTION_TABLE_NAME = "subscription";


    public static final String SUBSCRIPTION_COLUMN_SUBSCRIPTION_ID = "id";
    private static final String SUBSCRIPTION_COLUMN_SUBSCRIPTION_TITLE = "title";
    private static final String SUBSCRIPTION_COLUMN_SUBSCRIPTION_END_DATE = "end_date";
    private static final String SUBSCRIPTION_COLUMN_SUBSCRIPTION_GRACE_DATE = "grace_period_end_date";
    private static final String SUBSCRIPTION_COLUMN_SUBSCRIPTION_IS_CANCELLED = "is_cancelled";
    public static final String SUBSCRIPTION_COLUMN_SUBSCRIPTION_IS_EXPIRED = "is_expired";

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS "
            + SUBSCRIPTION_TABLE_NAME + "("
            + SUBSCRIPTION_COLUMN_SUBSCRIPTION_ID + " LONG PRIMARY KEY, "
            + SUBSCRIPTION_COLUMN_SUBSCRIPTION_TITLE + " VARCHAR(100), "
            + SUBSCRIPTION_COLUMN_SUBSCRIPTION_END_DATE + " VARCHAR(50), "
            + SUBSCRIPTION_COLUMN_SUBSCRIPTION_GRACE_DATE + " VARCHAR(50), "
            + SUBSCRIPTION_COLUMN_SUBSCRIPTION_IS_CANCELLED + " TEXT, "
            + SUBSCRIPTION_COLUMN_SUBSCRIPTION_IS_EXPIRED + " TEXT)";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + SUBSCRIPTION_TABLE_NAME;

    private static ContentValues getSubscriptionContentValues(Subscription subscription) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SUBSCRIPTION_COLUMN_SUBSCRIPTION_ID, subscription.getId());
        contentValues.put(SUBSCRIPTION_COLUMN_SUBSCRIPTION_TITLE, subscription.getTitle());
        contentValues.put(SUBSCRIPTION_COLUMN_SUBSCRIPTION_END_DATE, subscription.getEndDate());
        contentValues.put(SUBSCRIPTION_COLUMN_SUBSCRIPTION_GRACE_DATE, subscription.getGracePeriodEndDate());
        contentValues.put(SUBSCRIPTION_COLUMN_SUBSCRIPTION_IS_CANCELLED, String.valueOf(subscription.isIsCancelled()));
        contentValues.put(SUBSCRIPTION_COLUMN_SUBSCRIPTION_IS_EXPIRED, String.valueOf(subscription.isExpired()));
        return contentValues;
    }

    public static long insertSubscription(Subscription subscription) {
        return EbookDbAdapter.saveSubscription(getSubscriptionContentValues(subscription));
    }

    public static boolean checkSubscriptionIsExits(Long subscriptionId) {
        boolean isExits;
        Cursor cursor = EbookDbAdapter.getSubscription(subscriptionId);
        isExits = cursor.getCount() > 0;
        cursor.close();
        return isExits;
    }

    public static List<Subscription> getListSubscription() {
        Cursor cursor = EbookDbAdapter.getListSubscription();
        List<Subscription> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Subscription subscription = new Subscription();
                subscription.setId(cursor.getLong(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_ID)));
                subscription.setTitle(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_TITLE)));
                subscription.setEndDate(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_END_DATE)));
                subscription.setGracePeriodEndDate(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_GRACE_DATE)));
                subscription.setIsCancelled(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_IS_CANCELLED))));
                subscription.setExpired(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_IS_EXPIRED))));
                list.add(subscription);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static void updateSubscription(Subscription subscription) {
        EbookDbAdapter.updateSubscription(getSubscriptionContentValues(subscription), subscription.getId());
    }

    public static List<Subscription> getExpiredSubscriptions() {
        Cursor cursor = EbookDbAdapter.getExpiredSubscriptions();
        List<Subscription> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Subscription subscription = new Subscription();
                subscription.setId(cursor.getLong(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_ID)));
                subscription.setTitle(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_TITLE)));
                subscription.setEndDate(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_END_DATE)));
                subscription.setGracePeriodEndDate(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_GRACE_DATE)));
                subscription.setIsCancelled(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_IS_CANCELLED))));
                subscription.setExpired(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_IS_EXPIRED))));
                list.add(subscription);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static List<Subscription> getSubscriptions() {
        Cursor cursor = EbookDbAdapter.getSubscriptions();
        List<Subscription> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Subscription subscription = new Subscription();
                subscription.setId(cursor.getLong(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_ID)));
                subscription.setTitle(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_TITLE)));
                subscription.setEndDate(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_END_DATE)));
                subscription.setGracePeriodEndDate(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_GRACE_DATE)));
                subscription.setIsCancelled(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_IS_CANCELLED))));
                subscription.setExpired(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(SUBSCRIPTION_COLUMN_SUBSCRIPTION_IS_EXPIRED))));
                list.add(subscription);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static void deleteSubscription(Subscription localSubscription) {
        EbookDbAdapter.deleteSubscription(localSubscription.getId());
    }
}
