package com.chrismsolutions.chrismmultitimer;

import android.content.Context;
import android.database.Cursor;

import com.chrismsolutions.chrismmultitimer.data.MultiTimerContract.MultiTimerEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christian Myrvold on 13.11.2017.
 */

class MultiTimerUtil
{

    public static List<MultiTimer> fetchMultiTimers(Cursor mCursor, Context context)
    {
        ArrayList<MultiTimer> multiTimers = new ArrayList<MultiTimer>();

        while (mCursor.moveToNext())
        {
            String timerName = mCursor.getString(mCursor.getColumnIndexOrThrow(MultiTimerEntry.COLUMN_TIMER_NAME));
            String timerInfo = mCursor.getString(mCursor.getColumnIndexOrThrow(MultiTimerEntry.COLUMN_TIMER_INFO));
            long timerClock  = mCursor.getLong(mCursor.getColumnIndexOrThrow(MultiTimerEntry.COLUMN_TIMER_CLOCK));
            int id = mCursor.getInt(mCursor.getColumnIndexOrThrow(MultiTimerEntry._ID));

            MultiTimer multiTimer = new MultiTimer(timerName, timerInfo, timerClock, context, id);
            multiTimers.add(multiTimer);
        }

        return multiTimers;
    }
}
