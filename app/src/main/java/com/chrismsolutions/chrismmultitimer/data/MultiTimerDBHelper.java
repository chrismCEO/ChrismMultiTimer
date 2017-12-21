package com.chrismsolutions.chrismmultitimer.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.chrismsolutions.chrismmultitimer.data.MultiTimerContract.MultiTimerEntry;

/**
 * Created by Christian Myrvold on 14.11.2017.
 */

public class MultiTimerDBHelper extends SQLiteOpenHelper
{
    private static final String DB_NAME = "multitimers.db";
    private static final int DB_VERSION = 3;

    private static final String SQL_CREATE_TIMER_TABLE =
            "CREATE TABLE " + MultiTimerEntry.TABLE_NAME_TIMERS + " ("
            + MultiTimerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MultiTimerEntry.COLUMN_TIMER_NAME + " TEXT NOT NULL, "
            + MultiTimerEntry.COLUMN_TIMER_CLOCK + " BIGINT, "
            + MultiTimerEntry.COLUMN_TIMER_SHOW + " INT DEFAULT 0, "
            + MultiTimerEntry.COLUMN_TIMER_INFO + " TEXT);";

    private static final String SQL_ALTER_TABLE_INFO =
            "ALTER TABLE " +
            MultiTimerEntry.TABLE_NAME_TIMERS +
            " ADD COLUMN " + MultiTimerEntry.COLUMN_TIMER_INFO + " TEXT;";

    public MultiTimerDBHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(SQL_CREATE_TIMER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        //DB_VERSION = 3 (Added Info column)
        if (oldVersion < 3)
        {
            sqLiteDatabase.execSQL(SQL_ALTER_TABLE_INFO);
        }
        //onCreate(sqLiteDatabase);
    }
}
