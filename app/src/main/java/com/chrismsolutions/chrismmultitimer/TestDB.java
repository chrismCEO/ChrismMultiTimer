package com.chrismsolutions.chrismmultitimer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.chrismsolutions.chrismmultitimer.data.*;
import com.chrismsolutions.chrismmultitimer.data.MultiTimerContract.MultiTimerEntry;

/**
 * Created by Christian Myrvold on 14.11.2017.
 */

public class TestDB
{
    private static String LOG_TAG = TestDB.class.getName();

    public static void testDB(Context context)
    {
        MultiTimerDBHelper mDbHelper = new MultiTimerDBHelper(context);
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        //Show all multitimers
        String[] projection = {
                MultiTimerContract.MultiTimerEntry._ID,
                MultiTimerContract.MultiTimerEntry.COLUMN_TIMER_NAME,
                MultiTimerContract.MultiTimerEntry.COLUMN_TIMER_CLOCK,
                MultiTimerContract.MultiTimerEntry.COLUMN_TIMER_SHOW
        };


        Cursor cursor = database.query(
                MultiTimerEntry.TABLE_NAME_TIMERS,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        Log.i(LOG_TAG, "ALL MULTITIMERS");
        while (cursor.moveToNext())
        {
            Log.i(LOG_TAG, cursor.getString(cursor.getColumnIndexOrThrow(MultiTimerEntry._ID)));
            Log.i(LOG_TAG, cursor.getString(cursor.getColumnIndexOrThrow(MultiTimerEntry.COLUMN_TIMER_NAME)));
            Log.i(LOG_TAG, String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(MultiTimerEntry.COLUMN_TIMER_CLOCK))));
            Log.i(LOG_TAG, String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(MultiTimerEntry.COLUMN_TIMER_SHOW))));
            Log.i(LOG_TAG, "-------------------------------");
        }




        String selection = MultiTimerContract.MultiTimerEntry.COLUMN_TIMER_SHOW + MultiTimerProvider.getSqlJoker();
        String[] selectionArgs = new String[]{"1"};
        String sortOrder = MultiTimerContract.MultiTimerEntry.COLUMN_TIMER_NAME + " ASC";

    }
}
