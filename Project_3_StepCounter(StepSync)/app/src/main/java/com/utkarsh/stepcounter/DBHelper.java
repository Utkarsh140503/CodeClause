package com.utkarsh.stepcounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "StepCounterDB";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_STEP_COUNT = "step_count";

    // Table columns
    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "date";
    private static final String KEY_TIME = "time"; // Add time column
    private static final String KEY_STEP_COUNT = "step_count";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_STEP_COUNT_TABLE = "CREATE TABLE " + TABLE_STEP_COUNT + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DATE + " TEXT,"
                + KEY_TIME + " TEXT," // Add time column
                + KEY_STEP_COUNT + " INTEGER"
                + ")";
        db.execSQL(CREATE_STEP_COUNT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if it exists and create a new one
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STEP_COUNT);
        onCreate(db);
    }

    // Insert a new step count record with date, time, and step count
    public void insertStepData(String date, String time, int stepCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DATE, date);
        values.put(KEY_TIME, time); // Insert time
        values.put(KEY_STEP_COUNT, stepCount);

        // Insert the row
        db.insert(TABLE_STEP_COUNT, null, values);
        db.close();
    }

    // Get all step count records
    public ArrayList<BarDataModel> getAllStepCounts() {
        ArrayList<BarDataModel> dataModels = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_STEP_COUNT;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Loop through all rows and add to list
        if (cursor.moveToFirst()) {
            do {
                BarDataModel dataModel = new BarDataModel();
                dataModel.setId(cursor.getInt(0));
                dataModel.setDate(cursor.getString(1));
                dataModel.setTime(cursor.getString(2)); // Retrieve time
                dataModel.setTotalSteps(cursor.getInt(3));
                dataModels.add(dataModel);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return dataModels;
    }

    // Clear all step count data
    public void clearStepData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_STEP_COUNT);
        db.close();
    }
}