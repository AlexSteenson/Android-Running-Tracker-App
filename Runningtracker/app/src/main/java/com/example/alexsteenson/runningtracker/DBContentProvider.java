package com.example.alexsteenson.runningtracker;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.content.UriMatcher;
import android.content.ContentUris;


public class DBContentProvider extends ContentProvider {

    DBHelper dbHelper;
    SQLiteDatabase db;
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(DBProviderContract.AUTHORITY, "allRuns", 1);
    }

    @Override
    public boolean onCreate() {
        // Create dbHelper
        this.dbHelper = new DBHelper(this.getContext());
        db = dbHelper.getWritableDatabase();

        // If the table doesn't exist, create it
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+"allRuns"+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()<1) {
                this.dbHelper.onCreate(db);
                cursor.close();
            }
            cursor.close();
        }
        return true;
    }

    // Content code for querying the database
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return db.query("allRuns", projection, selection, selectionArgs, null, null,
                sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        if (uri.getLastPathSegment()==null) {
            return "vnd.android.cursor.dir/DBContentProvider.data.text";
        }else {
            return "vnd.android.cursor.item/DBContentProvider.data.text";
        }
    }

    // Content code for adding an entry into the table
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName = "allRuns";

        long id = db.insert(tableName, null, values);
        Uri nu = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(nu, null);
        return nu;
    }

    // Content code for deleting an entry
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        db.delete("allRuns", selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return 0;
    }

    // Content code for updating an entry
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        db.update("allRuns", values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return 0;
    }
}