package com.folioreader.model.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sap_press.rheinwerk_reader.download.datamanager.tables.LastReadTable;
import com.sap_press.rheinwerk_reader.download.datamanager.tables.LibraryTable;

import com.sap_press.rheinwerk_reader.logging.FolioLogging;

public class FolioDatabaseHelper extends SQLiteOpenHelper {
    @SuppressWarnings("unused")
    private static final String TAG = "FolioDatabaseHelper";

    private static FolioDatabaseHelper mInstance;
    private static SQLiteDatabase myWritableDb;

    public static final String DATABASE_NAME = "FolioReader.db";
    private static final int DATABASE_VERSION = 4;

    public static final String KEY_ID = "_id";
    private final Context mContext;

    public FolioDatabaseHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static FolioDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FolioDatabaseHelper(context);
        }
        return mInstance;
    }

    public static void clearInstance() {
        mInstance = null;
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
        FolioLogging.tag(TAG).d("create table highlight", "****" + HighLightTable.SQL_CREATE);
        db.execSQL(HighLightTable.SQL_CREATE);
        db.execSQL(ReadPositionTable.SQL_CREATE);
    }

    @Override
    public final void onUpgrade(final SQLiteDatabase db, final int oldVersion,
                                final int newVersion) {
		/* PROTECTED REGION ID(DatabaseUpdate) ENABLED START */

        // TODO Implement your database update functionality here and remove the
        // following method call!
        onUpgradeDropTables(db);
        onCreate(db);
        resetAllPreferences(mContext);

		/* PROTECTED REGION END */
    }

    /**
     * This basic upgrade functionality will destroy all old data on upgrade
     */
    private final void onUpgradeDropTables(final SQLiteDatabase db) {
        db.execSQL(HighLightTable.SQL_DROP);
        db.execSQL(LastReadTable.SQL_DROP);
    }

    /**
     * Resets all shared preferences
     *
     * @param context
     */
    private final void resetAllPreferences(Context context) {

    }
}
