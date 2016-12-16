package com.yeoman.simplestnote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SimplestNoteDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SimplestNote.db";

    public SimplestNoteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                FeedEntry._ID + " INTEGER PRIMARY KEY," +
                FeedEntry.COLUMN_NAME_TITLE + " TEXT," +
                FeedEntry.COLUMN_NAME_SUBTITLE + " TEXT )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
