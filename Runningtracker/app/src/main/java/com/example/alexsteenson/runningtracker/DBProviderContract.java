package com.example.alexsteenson.runningtracker;

import android.net.Uri;

public class DBProviderContract {
    public static final String AUTHORITY = "com.example.alexsteenson.runningtracker.DBContentProvider";
    public static final Uri WORKOUT_URI = Uri.parse("content://"+AUTHORITY+"/allRuns");
    public static final String _ID = "_id";
    public static final String TYPE = "type";
    public static final String TIME = "time";
    public static final String DISTANCE = "distance";
    public static final String TIMEDATE = "timeDate";
    public static final String DATE = "date";
    public static final String NAME = "name";
}
