package com.yeoman.simplestnote;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd HH:mm:ss");
        final SimplestNoteDbHelper mDbHelper = new SimplestNoteDbHelper(this);
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final EditText input = (EditText) findViewById(R.id.input);
        final TextView show = (TextView) findViewById(R.id.show);
        show.setText("");
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String strInput = input.getText().toString();
                String strShow = show.getText().toString();
                strShow = strInput + "\n" + strShow;
                show.setText(strShow);
                ContentValues mValues = new ContentValues();
                mValues.put(FeedEntry.COLUMN_NAME_TITLE, strInput);
                mValues.put(FeedEntry.COLUMN_NAME_SUBTITLE, dateFormatter.format(new Date()));
                db.insert(FeedEntry.TABLE_NAME, null, mValues);
                input.setText("");
                return false;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(MainActivity.this, ShowActivity.class);
                startActivity(mIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

