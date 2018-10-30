package com.sap_press.rheinwerk_reader.download.datamanager.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sap_press.rheinwerk_reader.download.datamanager.tables.LastReadTable;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.LibraryTable;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.SubscriptionTable;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.UserTable;

public class EbookDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "EbookDbHelper";
    private static SQLiteDatabase myWritableDb;
    private Context mContext;
    private static EbookDbHelper mInstance;

    private static final String DB_NAME = "EbookDB";
    private static final int VERSION = 23;

    public EbookDbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        mContext = context;
    }


    public static EbookDbHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new EbookDbHelper(context);
        }
        return mInstance;
    }

    public SQLiteDatabase getMyWritableDatabase() {
        if ((myWritableDb == null) || (!myWritableDb.isOpen())) {
            myWritableDb = this.getWritableDatabase();
        }

        return myWritableDb;
    }

    @Override
    public void close() {
        super.close();
        if (myWritableDb != null) {
            myWritableDb.close();
            myWritableDb = null;
        }
    }

    @Override
    public final void onCreate(final SQLiteDatabase db) {
        db.execSQL(UserTable.SQL_CREATE);
        db.execSQL(LibraryTable.SQL_CREATE);
        db.execSQL(SubscriptionTable.SQL_CREATE);
        db.execSQL(LastReadTable.SQL_CREATE);
    }

    @Override
    public final void onUpgrade(final SQLiteDatabase db, final int oldVersion,
                                final int newVersion) {
        onUpgradeDropTables(db);
    }

    /**
     * This basic upgrade functionality will destroy all old data on upgrade
     */
    private void onUpgradeDropTables(final SQLiteDatabase db) {
        Log.e(TAG, "onUpgrade: >>>");
        db.execSQL(UserTable.SQL_DROP);
        db.execSQL(LibraryTable.SQL_DROP);
        db.execSQL(SubscriptionTable.SQL_DROP);
        db.execSQL(LastReadTable.SQL_DROP);
        onCreate(db);
    }

}