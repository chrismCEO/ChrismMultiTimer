package com.chrismsolutions.chrismmultitimer;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.chrismsolutions.chrismmultitimer.data.MultiTimerContract.MultiTimerEntry;

/**
 * Created by Christian Myrvold on 22.11.2017.
 */

public class MultiTimerHiddenCursorAdapter extends CursorAdapter
{
    private SparseBooleanArray checkStates;

    MultiTimerHiddenCursorAdapter(Context context, Cursor cursor)
    {
        super(context, cursor, 0);
        checkStates = new SparseBooleanArray();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return LayoutInflater.from(context).inflate(R.layout.list_item_hidden_timers, parent, false);
    }

    /**
     * Set the data for the hidden timers and see if the user checks them or not
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        TextView timerName = view.findViewById(R.id.hidden_timer_name);
        final String name = cursor.getString(cursor.getColumnIndexOrThrow(MultiTimerEntry.COLUMN_TIMER_NAME));
        timerName.setText(name);

        TextView timerClock = view.findViewById(R.id.hidden_timer_clock);
        int clockMillis = cursor.getInt(cursor.getColumnIndexOrThrow(MultiTimerEntry.COLUMN_TIMER_CLOCK));
        String info = cursor.getString(cursor.getColumnIndexOrThrow(MultiTimerEntry.COLUMN_TIMER_INFO));
        final String clockString = new MultiTimer(name, info, clockMillis, context, 0).toString();
        timerClock.setText(clockString);

        TextView timerId = view.findViewById(R.id.hidden_timer_id);
        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(MultiTimerEntry._ID));
        timerId.setText(String.valueOf(id));

        checkStates.put(id, false);
        final CheckBox checkBox = view.findViewById(R.id.hidden_timer_checkbox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkStates.put(id, checkBox.isChecked());
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBox.setChecked(!checkBox.isChecked());
                checkStates.put(id, checkBox.isChecked());
            }
        });
    }

    public boolean getChecked(int id)
    {
        return checkStates.get(id);
    }

    SparseBooleanArray getCheckStates()
    {
        return checkStates;
    }
}
