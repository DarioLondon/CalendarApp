package com.darioguida.calendarapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.HashMap;


/**
 * Created by Dario on 29/03/2016.
 */
public class EventDatabase {
    public EventDatabase() {
    }

    public static abstract class Events implements BaseColumns {

        public static final String TABLE_NAME = "event";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_ENTRY_ID = "eventId";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
    }

}

class EventsDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Events.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + EventDatabase.Events.TABLE_NAME + " ( " +
                    EventDatabase.Events.COLUMN_NAME_ID + "  INTEGER PRIMARY KEY autoincrement ," +
                    EventDatabase.Events.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    EventDatabase.Events.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    EventDatabase.Events.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    EventDatabase.Events.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    EventDatabase.Events.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + " );";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + EventDatabase.Events.TABLE_NAME;

    public EventsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_ENTRIES);
        System.out.println("db created");

    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
        onCreate(db);
    }

    public void insert(HashMap<String, String> data) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EventDatabase.Events.COLUMN_NAME_ENTRY_ID, data.get("id"));
        values.put(EventDatabase.Events.COLUMN_NAME_TITLE, data.get("title").trim());
        values.put(EventDatabase.Events.COLUMN_NAME_TIME, data.get("time"));
        values.put(EventDatabase.Events.COLUMN_NAME_DATE, data.get("date").trim());
        values.put(EventDatabase.Events.COLUMN_NAME_DESCRIPTION, data.get("content"));


        long newRowId;
        newRowId = db.insert(
                EventDatabase.Events.TABLE_NAME,
                null,
                values);

    }

    public Cursor get(String date) {


        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                EventDatabase.Events.COLUMN_NAME_ID,
                EventDatabase.Events.COLUMN_NAME_ENTRY_ID,
                EventDatabase.Events.COLUMN_NAME_TITLE,
                EventDatabase.Events.COLUMN_NAME_TIME,
                EventDatabase.Events.COLUMN_NAME_DATE,
                EventDatabase.Events.COLUMN_NAME_DESCRIPTION
        };

        String selection = EventDatabase.Events.COLUMN_NAME_DATE + " LIKE ?";
        String[] args = {date};
        Cursor c = db.query(
                EventDatabase.Events.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                args,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null
        );


        return c;

    }

    public boolean checkDuplicateTitle(String title, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean result = false;
        String[] projection = {
                EventDatabase.Events.COLUMN_NAME_ID,
                EventDatabase.Events.COLUMN_NAME_ENTRY_ID,
                EventDatabase.Events.COLUMN_NAME_TITLE,
                EventDatabase.Events.COLUMN_NAME_TIME,
                EventDatabase.Events.COLUMN_NAME_DATE,
                EventDatabase.Events.COLUMN_NAME_DESCRIPTION

        };


        Cursor c = db.query(
                EventDatabase.Events.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null
        );


        c.moveToFirst();

        while (c.moveToNext()) {
            if (c.getString(c.getColumnIndex("title")).equals(title) && c.getString(c.getColumnIndex("date")).equals(date))
                result = true;
        }
        Log.d("Function Title equal", "" + result);

        return result;
    }

    public void updateAppoinment(HashMap<String, String> data) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EventDatabase.Events.COLUMN_NAME_ENTRY_ID, data.get("title") + "@" + data.get("date"));
        values.put(EventDatabase.Events.COLUMN_NAME_TITLE, data.get("title"));
        values.put(EventDatabase.Events.COLUMN_NAME_TIME, data.get("time"));
        values.put(EventDatabase.Events.COLUMN_NAME_DATE, data.get("date"));
        values.put(EventDatabase.Events.COLUMN_NAME_DESCRIPTION, data.get("content"));
        String id = data.get("id");
        String selection = EventDatabase.Events.COLUMN_NAME_ENTRY_ID + " = ?";
        String[] selectionArgs = {id};

        int count = db.update(
                EventDatabase.Events.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public void deleteSingle(String val) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = EventDatabase.Events.COLUMN_NAME_ENTRY_ID + " = ?";
        String[] selectionArgs = {val};

        int count = db.delete(
                EventDatabase.Events.TABLE_NAME,
                selection,
                selectionArgs);
        db.close();
    }

    public void deleteAll(String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = EventDatabase.Events.COLUMN_NAME_DATE + " = ?";
        String[] selectionArgs = {date};

        db.delete(
                EventDatabase.Events.TABLE_NAME,
                selection,
                selectionArgs);

        db.close();
    }



}
//TODO order to remember : 0=> eventid | 1=> title | 2=> time | 3=> date | 4=> description