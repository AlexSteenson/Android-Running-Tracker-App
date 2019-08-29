package com.example.alexsteenson.runningtracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    // Constructor
    public DBHelper(Context context) {
        super(context, "runnerDB", null, 1);
    }

    // Create database table
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("Database Created", "True");
        db.execSQL("CREATE TABLE " + "allRuns" + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "type" + " TEXT, " + "distance" + " FLOAT, " + "time" + " INTEGER, " + "timeDate" + " TEXT, " + "date" + " TEXT, " + "name" + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}