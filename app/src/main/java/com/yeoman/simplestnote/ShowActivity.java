package com.yeoman.simplestnote;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.telly.floatingaction.FloatingAction;

import java.util.ArrayList;
import java.util.List;

public class ShowActivity extends AppCompatActivity implements View.OnClickListener{
    private FloatingAction mFloatingAction;
    private SQLiteDatabase mdb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        final ListView list = (ListView) findViewById(R.id.list);
        final SimplestNoteDbHelper mDbHelper = new SimplestNoteDbHelper(this);
        final SQLiteDatabase mdb = mDbHelper.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + FeedEntry.TABLE_NAME + " ORDER BY " + FeedEntry._ID + " DESC;";
        Cursor cursor = mdb.rawQuery(selectQuery, null);
        list.setAdapter(new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[]{FeedEntry.COLUMN_NAME_TITLE, FeedEntry.COLUMN_NAME_SUBTITLE},
                new int[]{android.R.id.text1,android.R.id.text2}));
        mFloatingAction = FloatingAction.from(this)
                .listenTo(list)
                .colorResId(R.color.colorAccent)
                .icon(android.R.drawable.ic_delete)
                .listener(this)
                .build();
        ImageButton btn = (ImageButton) findViewById(R.id.fa_action_view);
        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mdb.delete(FeedEntry.TABLE_NAME,null,null);
                finish();
                return true;
            }
        });
    }

    @Override
    public void onClick(View v){
        finish();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mFloatingAction.onDestroy();
    }
}
