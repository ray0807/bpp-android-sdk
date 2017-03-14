package com.xmtj.bpgdecoder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by wanglei on 28/02/17.
 */

public class DBHelperManager {

    private static final String TAG = "DBHelperManager";

    private static final String DATABASE_NAME = "bpg_count.db";
    private static final int DATABASE_VERSION = 1;


    // Variable to hold the database instance
    protected SQLiteDatabase mDb;
    // Context of the application using the database.
    private final Context mContext;
    // Database open/upgrade helper
    private DbHelper mDbHelper;

    public DBHelperManager(Context context) {
        mContext = context;
        mDbHelper = new DbHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DBHelperManager open() throws SQLException {
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDb.close();
    }

    public static final String ROW_ID = "_id";


    // -------------- EVENT DEFINITIONS ------------
    public static final String BPG_TABLE = "BPG_TABLE";

    public static final String KEY_COLUMN = "bpg_key";
    public static final String COUNT_COLUMN = "shot_count";


    // -------- TABLES CREATION ----------


    // Event CREATION
    private static final String DATABASE_EVENT_CREATE = "create table " + BPG_TABLE + " (" +
            "_id integer primary key autoincrement, " +
            KEY_COLUMN + " text, " +
            COUNT_COLUMN + " integer" +
            ")";


    // ----------------Event HELPERS --------------------
    public long addBpgCount(long key, int count) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_COLUMN, key + "");
        contentValues.put(COUNT_COLUMN, count);
        return mDb.insert(BPG_TABLE, null, contentValues);
    }

    public long updateBpgCount(long key, int count) {
        String where = KEY_COLUMN + " = " + key;
        ContentValues contentValues = new ContentValues();
        contentValues.put(COUNT_COLUMN, count);
        return mDb.update(BPG_TABLE, contentValues, where, null);
    }

    public boolean removeBpgCount(long rowIndex) {
        return mDb.delete(BPG_TABLE, ROW_ID + " = " + rowIndex, null) > 0;
    }

    public boolean removeAllBpgCount() {
        return mDb.delete(BPG_TABLE, null, null) > 0;
    }

    public Cursor getAllBpgCount() {
        return mDb.query(BPG_TABLE, new String[]{
                KEY_COLUMN,
                COUNT_COLUMN
        }, null, null, null, null, null);
    }

    public Cursor getBpgCount(long key) {
        Cursor res = mDb.query(BPG_TABLE, new String[]{
                KEY_COLUMN,
                COUNT_COLUMN
        }, KEY_COLUMN + " = " + key, null, null, null, null);

        if (res != null) {
            res.moveToFirst();
        }
        return res;
    }




    private static class DbHelper extends SQLiteOpenHelper {
        public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // Called when no database exists in disk and the helper class needs
        // to create a new one.
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_EVENT_CREATE);

        }

        // Called when there is a database version mismatch meaning that the version
        // of the database on disk needs to be upgraded to the current version.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Log the version upgrade.
            Log.w(TAG, "Upgrading from version " +
                    oldVersion + " to " +
                    newVersion + ", which will destroy all old data");

            // Upgrade the existing database to conform to the new version. Multiple
            // previous versions can be handled by comparing _oldVersion and _newVersion
            // values.

            // The simplest case is to drop the old table and create a new one.
            db.execSQL("DROP TABLE IF EXISTS " + BPG_TABLE + ";");

            // Create a new one.
            onCreate(db);
        }
    }
}
