package com.yeoman.simplestnote;

import android.provider.BaseColumns;

public  class FeedEntry implements BaseColumns {
    public static final String TABLE_NAME = "notes";
    public static final String CONTENT = "content";
    public static final String TIME = "time";
    public static final String FLAG="flag";
    public static final int Exist = 0;
    public static final int Del = 1;
    public static final String SelectALL="SELECT  * FROM " + TABLE_NAME +
            " WHERE " + FLAG + " =0" +
            " ORDER BY " + _ID + " DESC;";
}
