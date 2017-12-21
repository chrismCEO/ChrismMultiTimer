package com.chrismsolutions.chrismmultitimer.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.security.PrivilegedAction;

/**
 * Created by Christian Myrvold on 14.11.2017.
 */

public final class MultiTimerContract
{
    //Content values
    public static final String CONTENT_AUTHORITY = "com.chrismsolutions.chrismmultitimer";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MULTITIMER_TIMERS = "timers";

    private MultiTimerContract()
    {
        throw new AssertionError("MultiTimerContract cannot be instantiated");
    }

    public static class MultiTimerEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI_TIMER = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MULTITIMER_TIMERS);
        public static final String CONTENT_LIST_TYPE_TIMER =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MULTITIMER_TIMERS;

        public static final String CONTENT_ITEM_TYPE_TIMER =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MULTITIMER_TIMERS;

        //Timer table and column names
        public static final String TABLE_NAME_TIMERS = "timers";
        public static final String COLUMN_TIMER_NAME = "name";
        public static final String COLUMN_TIMER_CLOCK = "clock";
        public static final String COLUMN_TIMER_SHOW = "show";
        public static final String COLUMN_TIMER_INFO = "info";

        public static final int VALUE_TIMER_SHOW_TRUE = 1;
        public static final int VALUE_TIMER_SHOW_FALSE = 0;
    }
}
