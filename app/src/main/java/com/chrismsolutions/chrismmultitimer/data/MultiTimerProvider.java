package com.chrismsolutions.chrismmultitimer.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.chrismsolutions.chrismmultitimer.MainActivity;
import com.chrismsolutions.chrismmultitimer.R;
import com.chrismsolutions.chrismmultitimer.data.MultiTimerContract.MultiTimerEntry;

/**
 * Created by Christian Myrvold on 14.11.2017.
 */

public class MultiTimerProvider extends ContentProvider
{
    public static final String LOG_TAG = MultiTimerProvider.class.getName();
    private MultiTimerDBHelper mDBHelper;
    private static final String SQL_JOKER = "=?";

    private static final int TIMERS = 100;
    private static final int TIMER_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        sUriMatcher.addURI(MultiTimerContract.CONTENT_AUTHORITY, MultiTimerContract.PATH_MULTITIMER_TIMERS, TIMERS);
        sUriMatcher.addURI(MultiTimerContract.CONTENT_AUTHORITY, MultiTimerContract.PATH_MULTITIMER_TIMERS + "/#", TIMER_ID);
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new MultiTimerDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder)
    {
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        Cursor cursor = null;

        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case TIMERS:
                cursor = database.query(
                        MultiTimerEntry.TABLE_NAME_TIMERS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case TIMER_ID:
                selection = MultiTimerEntry._ID + SQL_JOKER;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(
                        MultiTimerEntry.TABLE_NAME_TIMERS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri)
    {
        String type = "";
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case TIMERS:
                type = MultiTimerEntry.CONTENT_LIST_TYPE_TIMER;
                break;

            case TIMER_ID:
                type = MultiTimerEntry.CONTENT_ITEM_TYPE_TIMER;
                break;

            default:
                throw new IllegalStateException("Unknown URI" + uri + " with match " + match);
        }
        return type;
    }

    private boolean checkValues(ContentValues contentValues)
    {
        boolean ok = true;

        /*String timerName = contentValues.getAsString(MultiTimerEntry.COLUMN_TIMER_NAME);

        if (contentValues.containsKey(MultiTimerEntry.COLUMN_TIMER_NAME) &&
                TextUtils.isEmpty(timerName))
        {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.empty_timer_name_check),
                    Toast.LENGTH_SHORT).show();

            ok = false;
        }*/

        return ok;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        Uri uriLocal = null;
        if (contentValues.size() > 0 && checkValues(contentValues))
        {
            final int match = sUriMatcher.match(uri);
            switch (match)
            {
                case TIMERS:
                    uriLocal = insertTimer(uri, contentValues);
                    break;

                default:
                    throw new IllegalArgumentException("Cannot query unknown URI " + uri);
            }
        }
        return uriLocal;
    }

    private Uri insertTimer(Uri uri, ContentValues contentValues)
    {
        Uri uriLocal = null;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        long id = 0;

        id = database.insert(MultiTimerEntry.TABLE_NAME_TIMERS, null, contentValues);

        if (id == -1)
        {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
        }
        else
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues contentValues,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs)
    {
        int result = 0;
        if (contentValues.size() > 0 && checkValues(contentValues))
        {
            final int match = sUriMatcher.match(uri);
            switch (match)
            {
                case TIMERS:
                    result = updateTimer(uri, contentValues, selection, selectionArgs);
                    break;

                case TIMER_ID:
                    selection = MultiTimerEntry._ID + SQL_JOKER;
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    result = updateTimer(uri, contentValues, selection, selectionArgs);
                    break;

                default:
                    throw new IllegalArgumentException("Update is not supported for " + uri);
            }
        }
        return result;
    }

    private int updateTimer(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int result = 0;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        if (selectionArgs.length > 1)
        {
            selection = MultiTimerEntry._ID + " IN(?";
            for (int i = 1; i < selectionArgs.length; i++)
            {
                selection += ",?";
            }
            selection += ")";
        }

        result = database.update(
                MultiTimerEntry.TABLE_NAME_TIMERS,
                contentValues,
                selection,
                selectionArgs);

        if (result == -1)
        {
            Log.e(LOG_TAG, "Failed to update row for " + uri);
            result = 0;
        }

        if (result != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return result;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        int result = 0;

        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case TIMERS:
                result = database.delete(MultiTimerEntry.TABLE_NAME_TIMERS, selection, selectionArgs);
                break;

            case TIMER_ID:
                selection = MultiTimerEntry._ID + SQL_JOKER;
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                result = database.delete(MultiTimerEntry.TABLE_NAME_TIMERS, selection, selectionArgs);
                break;
        }

        if (result != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }

    public static String getSqlJoker()
    {
        return SQL_JOKER;
    }

}
