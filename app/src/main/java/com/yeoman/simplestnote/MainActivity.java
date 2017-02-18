package com.yeoman.simplestnote;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Intent.EXTRA_TEXT;

public class MainActivity extends AppCompatActivity {

    private KeyBackEditText input;
    private SQLiteDatabase db;
    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SimplestNoteDbHelper mDbHelper = new SimplestNoteDbHelper(this);
        db = mDbHelper.getWritableDatabase();
        list = (ListView) findViewById(R.id.list);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                saveNote(intent.getStringExtra(EXTRA_TEXT)); // Handle text being sent
            }
            finish();
        } else {
            refreshList();
        }
        input = (KeyBackEditText) findViewById(R.id.input);
        input.setHorizontallyScrolling(false);
        input.setMaxLines(Integer.MAX_VALUE);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                saveNote(input.getText().toString());
                refreshList();
                return true;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(list);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogDelete();
            }
        });

    }

    @Override
    public void onBackPressed(){
        saveNote(input.getText().toString());
        super.onBackPressed();
    }

    protected void saveNote(String strInput){
        SimpleDateFormat dateFormatter = new SimpleDateFormat(getString(R.string.dateFormat));
        if (strInput.equals("")) return;
        ContentValues mValues = new ContentValues();
        mValues.put(FeedEntry.CONTENT, strInput);
        mValues.put(FeedEntry.TIME, dateFormatter.format(new Date()));
        mValues.put(FeedEntry.FLAG, FeedEntry.Exist);
        db.insert(FeedEntry.TABLE_NAME, null, mValues);
        input.setText("");
    }

    protected void refreshList(){

        Cursor cursor = db.rawQuery(FeedEntry.SelectALL, null);
        list.setAdapter(new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[]{FeedEntry.CONTENT, FeedEntry.TIME},
                new int[]{android.R.id.text1,android.R.id.text2}));
    }

    protected void dialogDelete(){
        new AlertDialog.Builder(this)
                .setTitle("Delete all?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ContentValues mValues = new ContentValues();
                        mValues.put(FeedEntry.FLAG, FeedEntry.Del);
                        db.update(FeedEntry.TABLE_NAME,mValues,null,null);
                        refreshList();
                        undoSnack();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setNeutralButton("And share", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Cursor cursor = db.rawQuery(FeedEntry.SelectALL, null);
                        String str = "SimplestNote\n";
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()){
                            String content = cursor.getString(1);
                            String time = cursor.getString(2);
                            str += content + " at " + time + "\n";
                            cursor.moveToNext();
                        }
                        cursor.close();
                        shareText(str);
                        db.delete(FeedEntry.TABLE_NAME,null,null);
                        finish();
                    }
                })
                .create()
                .show();
    }

    protected void undoSnack(){
        Snackbar.make(list, "All entry deleted!", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContentValues mValues = new ContentValues();
                        mValues.put(FeedEntry.FLAG, FeedEntry.Exist);
                        db.update(FeedEntry.TABLE_NAME,mValues,null,null);
                        refreshList();
                    }
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event){
                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
                            finish();
                        else
                            db.delete(FeedEntry.TABLE_NAME,FeedEntry.FLAG + "=" + FeedEntry.Del,null);
                    }
                })
                .show();
    }

    protected void shareText(String str){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, str);
        intent.setType("text/plain");
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                }

            }, 100);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete) {
            dialogDelete();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

