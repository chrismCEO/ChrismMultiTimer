package com.chrismsolutions.chrismmultitimer;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

import com.chrismsolutions.chrismmultitimer.data.MultiTimerDBHelper;

import java.util.List;

/**
 * Created by Christian Myrvold on 13.11.2017.
 */

class MultiTimerLoader extends AsyncTaskLoader<List<MultiTimer>>
{
    Cursor mCursor;
    List<MultiTimer> multiTimers;

    public MultiTimerLoader(Context context, Cursor cursor)
    {
        super(context);
        mCursor = cursor;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<MultiTimer> loadInBackground() {
        if (mCursor != null && multiTimers == null)
        {
            multiTimers = MultiTimerUtil.fetchMultiTimers(mCursor, getContext());
        }
        return multiTimers;
    }
}
