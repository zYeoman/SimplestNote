package com.yeoman.simplestnote;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Intent.EXTRA_TEXT;

public class MainActivity extends AppCompatActivity {

    private KeyBackEditText input;
    private SQLiteDatabase db;
    private ListView list;
    private int mPreviousVisibleItem;
    private SharedPreferences mShared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SimplestNoteDbHelper mDbHelper = new SimplestNoteDbHelper(this);
        db = mDbHelper.getWritableDatabase();
        list = (ListView) findViewById(R.id.list);
        mShared = getSharedPreferences("lastnote", Activity.MODE_PRIVATE);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                saveNote(intent.getStringExtra(EXTRA_TEXT)); // Handle text being sent
                updateWidget();
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
        input.setText(mShared.getString("note", ""));
        input.setSelection(input.getText().length());

        FloatingActionButton pop_fab = (FloatingActionButton) findViewById(R.id.pop_fab);
        FloatingActionButton share_fab = (FloatingActionButton) findViewById(R.id.share_fab);
        final FloatingActionMenu fam = (FloatingActionMenu) findViewById(R.id.fam);

        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem > mPreviousVisibleItem) {
                    fam.hideMenuButton(true);
                } else if (firstVisibleItem < mPreviousVisibleItem) {
                    fam.showMenuButton(true);
                }
                mPreviousVisibleItem = firstVisibleItem;
            }
        });

        pop_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fam.close(true);
                Cursor cursor = db.rawQuery(FeedEntry.SelectALL, null);
                if(cursor.moveToFirst()) {
                    String rowId = cursor.getString(cursor.getColumnIndex(FeedEntry._ID));
                    String content = cursor.getString(1);
                    input.setText(content);
                    input.setSelection(content.length());
                    db.delete(FeedEntry.TABLE_NAME, FeedEntry._ID + "=?",  new String[]{rowId});
                }
                cursor.close();
                refreshList();
            }
        });

        share_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fam.close(true);
                shareText();
            }
        });
        sendText();
    }

    @Override
    public void onStop(){
        mShared.edit().putString("note", input.getText().toString()).apply();
        super.onStop();
    }

    @Override
    public void onPause() {
        InputMethodManager imm = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        super.onPause();
    }

    protected void saveNote(String strInput){
        SimpleDateFormat dateFormatter = new SimpleDateFormat(getString(R.string.dateFormat), Locale.CHINA);
        if (strInput.trim().equals("")) return;
        ContentValues mValues = new ContentValues();
        mValues.put(FeedEntry.CONTENT, strInput);
        mValues.put(FeedEntry.TIME, dateFormatter.format(new Date()));
        if (strInput.startsWith("!"))
            mValues.put(FeedEntry.FLAG, FeedEntry.Store);
        else
            mValues.put(FeedEntry.FLAG, FeedEntry.Exist);
        db.insert(FeedEntry.TABLE_NAME, null, mValues);
        input.setText("");
        mShared.edit().putString("note", "").apply();
    }

    protected void updateWidget(){
        Context context = this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.show_all_note);
        ComponentName thisWidget = new ComponentName(context, ShowAllNote.class);
        Cursor cursor = db.rawQuery(FeedEntry.SelectALL, null);
        // Instruct the widget manager to update the widget
        String content1="安身立命",content2="云淡风轻",content3="啦啦啦啦";
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            content1 = cursor.getString(1);
            cursor.moveToNext();
            if (!cursor.isAfterLast()) {
                content2 = cursor.getString(1);
                cursor.moveToNext();
                if (!cursor.isAfterLast()) {
                    content3 = cursor.getString(1);
                }
            }
        }
        views.setTextViewText(R.id.textView1, content1);
        views.setTextViewText(R.id.textView2, content2);
        views.setTextViewText(R.id.textView3, content3);
        cursor.close();
        appWidgetManager.updateAppWidget(thisWidget, views);
    }

    protected void refreshList(){
        Cursor cursor = db.rawQuery(FeedEntry.SelectALL, null);
        list.setAdapter(new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[]{FeedEntry.CONTENT, FeedEntry.TIME},
                new int[]{android.R.id.text1,android.R.id.text2},
                0));
    }

    protected void restorableDelete(){
        ContentValues mValues = new ContentValues();
        mValues.put(FeedEntry.FLAG, FeedEntry.Del);
        db.update(FeedEntry.TABLE_NAME,mValues,FeedEntry.FLAG+"="+FeedEntry.Exist,null);
        refreshList();
        undoSnack();
    }

    protected void undoSnack(){
        Snackbar.make(list, "All entry deleted!", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContentValues mValues = new ContentValues();
                        mValues.put(FeedEntry.FLAG, FeedEntry.Exist);
                        db.update(FeedEntry.TABLE_NAME,mValues,FeedEntry.FLAG+"="+FeedEntry.Del,null);
                        refreshList();
                    }
                })
                .show();
    }

    protected void shareText(){
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
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, str);
        intent.setType("text/plain");
        startActivity(intent);
    }

    protected void deleteAsk(){
        new AlertDialog.Builder(this)
                .setTitle("Save failure, delete anyway?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.delete(FeedEntry.TABLE_NAME,FeedEntry.toDel,null);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create()
                .show();
    }

    protected void sendText(){
        AVOSCloud.initialize(this,"8hgOr9Fackt9Y2TrVD8KAvnr-gzGzoHsz","XBGC5iyLjyNouI1L4skVJB1O");
        AVObject note = new AVObject("Note");
        Cursor cursor = db.rawQuery(FeedEntry.SelectDel, null);
        String str = "";
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            String content = cursor.getString(1);
            String time = cursor.getString(2);
            str += content + " at " + time + "\n";
            cursor.moveToNext();
        }
        cursor.close();

        if(str.trim().length() <= 0)return;
        note.put("content", str);
        note.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    // 保存成功
                    // 删除原来数据
                    db.delete(FeedEntry.TABLE_NAME,FeedEntry.toDel,null);
                }
                else {
                    // 保存失败
                    deleteAsk();
                }
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
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(input, InputMethodManager.SHOW_FORCED);
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
            restorableDelete();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

