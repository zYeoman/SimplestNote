package com.yeoman.simplestnote;

import android.provider.BaseColumns;

class FeedEntry implements BaseColumns {
    public static final String TABLE_NAME = "notes";
    public static final String CONTENT = "content";
    public static final String TIME = "time";
    public static final String FLAG="flag";
    public static final int Del = 0;
    public static final int Exist = 1;
    public static final int Store = 2;
    public static final String toDel = "flag="+Del;
    public static final String SelectALL="SELECT  * FROM " + TABLE_NAME +
            " WHERE " + FLAG + ">" + Del +
            " ORDER BY " + _ID + " DESC;";
    public static final String SelectDel="SELECT  * FROM " + TABLE_NAME +
            " WHERE " + FLAG + "=" + Del +
            " ORDER BY " + _ID + " DESC;";
}
