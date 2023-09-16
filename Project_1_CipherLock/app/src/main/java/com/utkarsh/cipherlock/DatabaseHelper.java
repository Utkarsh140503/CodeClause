package com.utkarsh.cipherlock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CipherLock.db";
    private static final int DATABASE_VERSION = 2; // Increase the version number

    // Define your table schema
    static final String TABLE_NAME = "Passwords";

    // Use BaseColumns._ID for the primary key
    static final String COLUMN_ID = BaseColumns._ID;
    static final String COLUMN_USERNAME = "username";
    static final String COLUMN_WEBSITE = "website";
    static final String COLUMN_PASSWORD = "password";
    static final String COLUMN_TIMESTAMP = "timestamp"; // Add the timestamp column
    static final String COLUMN_SAVED_LOCALLY = "SavedLocally";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT, " +
                    COLUMN_WEBSITE + " TEXT, " +
                    COLUMN_PASSWORD + " TEXT, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    COLUMN_SAVED_LOCALLY + " INTEGER);"; // Add SavedLocally column as INTEGER

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
