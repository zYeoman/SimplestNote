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

    private EditText input;
    private TextView show;
    private SimplestNoteDbHelper mDbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDbHelper = new SimplestNoteDbHelper(this);
        db = mDbHelper.getWritableDatabase();
        input = (EditText) findViewById(R.id.input);
        show = (TextView) findViewById(R.id.show);
        show.setText("");
        input.setHorizontallyScrolling(false);
        input.setMaxLines(Integer.MAX_VALUE);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                saveNote();
                return false;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
                finish();
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view){
                saveNote();
                Intent mIntent = new Intent(MainActivity.this, ShowActivity.class);
                startActivity(mIntent);
                return true;
            }
        });
        Intent mIntent = new Intent(MainActivity.this, ShowActivity.class);
        startActivity(mIntent);

    }

    protected void saveNote(){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd HH:mm:ss");
        String strInput = input.getText().toString();
        if (strInput.equals("")) return;
        String strShow = show.getText().toString();
        strShow = strInput + "\n" + strShow;
        show.setText(strShow);
        ContentValues mValues = new ContentValues();
        mValues.put(FeedEntry.COLUMN_NAME_TITLE, strInput);
        mValues.put(FeedEntry.COLUMN_NAME_SUBTITLE, dateFormatter.format(new Date()));
        db.insert(FeedEntry.TABLE_NAME, null, mValues);
        input.setText("");
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

