package com.yeoman.simplestnote;

import android.provider.BaseColumns;

public  class FeedEntry implements BaseColumns {
    public static final String TABLE_NAME = "notes";
    public static final String COLUMN_NAME_TITLE = "content";
    public static final String COLUMN_NAME_SUBTITLE = "time";
}
