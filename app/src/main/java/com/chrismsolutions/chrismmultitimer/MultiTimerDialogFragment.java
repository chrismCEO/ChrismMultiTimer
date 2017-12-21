package com.chrismsolutions.chrismmultitimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.chrismsolutions.chrismmultitimer.data.MultiTimerContract.MultiTimerEntry;
import com.chrismsolutions.chrismmultitimer.data.MultiTimerProvider;

import java.util.concurrent.TimeUnit;

/**
 * Created by Christian Myrvold on 14.11.2017.
 */

public class MultiTimerDialogFragment extends DialogFragment
{
    public static final String DIALOG_TAG = "multitimer_dialog";
    public static final java.lang.String DIALOG_EDIT = "DIALOG_EDIT";
    public static final java.lang.String DIALOG_TIMER_NAME = "DIALOG_TIMER_NAME";
    public static final java.lang.String DIALOG_TIME_MILLIS = "DIALOG_TIME_MILLIS";
    public static final java.lang.String DIALOG_TIME_ID = "DIALOG_TIME_ID";
    public static final String DIALOG_TIME_TIMER = "DIALOG_TIME_TIMER";

    private int hours, minutes, seconds;
    private long timeMillis;
    private int id;


    public MultiTimerDialogFragment()
    {
        super();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle arguments;
        final boolean edit;
        final Context context = getActivity();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        final MultiTimer timer;

        final String name;
        final NumberPicker numberPickerHours, numberPickerMinutes, numberPickerSeconds;

        View dialogView;
        final EditText timerNameView;

        //Get arguments
        if (getArguments() != null)
        {
            arguments = getArguments();
            edit = arguments.getBoolean(DIALOG_EDIT);
            name = arguments.getString(DIALOG_TIMER_NAME);
            timeMillis = arguments.getLong(DIALOG_TIME_MILLIS);
            id = arguments.getInt(DIALOG_TIME_ID);
            timer = (MultiTimer) arguments.getSerializable(DIALOG_TIME_TIMER);
        }
        else
        {
            edit = false;
            name = "";
            timeMillis = 0;
            id = 0;
            timer = null;
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();

        dialogView = inflater.inflate(R.layout.dialog_timer, null);
        dialogBuilder.setView(dialogView);

        timerNameView = (EditText) dialogView.findViewById(R.id.dialog_timer_name_edit);

        dialogView.findViewById(R.id.dialog_timer_name).setVisibility(View.GONE);
        dialogBuilder.setTitle(R.string.create_timer_dialog);

        //Define the numberpickers
        numberPickerHours = (NumberPicker) dialogView.findViewById(R.id.numberpicker_hours);
        numberPickerHours.setMinValue(0);
        numberPickerHours.setMaxValue(99);
        numberPickerHours.setWrapSelectorWheel(true);

        numberPickerHours.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                setHour(newValue);
            }
        });

        numberPickerMinutes = (NumberPicker) dialogView.findViewById(R.id.numberpicker_minutes);
        numberPickerMinutes.setMinValue(0);
        numberPickerMinutes.setMaxValue(60);
        numberPickerMinutes.setWrapSelectorWheel(true);

        numberPickerMinutes.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                setMinutes(newValue);
            }
        });

        numberPickerSeconds = (NumberPicker) dialogView.findViewById(R.id.numberpicker_seconds);
        numberPickerSeconds.setMinValue(0);
        numberPickerSeconds.setMaxValue(60);
        numberPickerSeconds.setWrapSelectorWheel(true);

        numberPickerSeconds.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                setSeconds(newValue);
            }
        });

        if (edit)
        {
            dialogBuilder.setTitle(context.getString(R.string.edit_timer_dialog));
            dialogView.findViewById(R.id.dialog_timer_name).setVisibility(View.VISIBLE);

            if (name != null)
            {
                timerNameView.setText(name);
                timerNameView.setSelection(name.length());
            }

            //Set the existing time
            int hours, minutes, seconds;
            int[] time = MultiTimer.convertMillisToTime(timeMillis);
            hours = time[MultiTimer.TIME_HOUR];
            minutes = time[MultiTimer.TIME_MINUTE];
            seconds = time[MultiTimer.TIME_SECOND];

            numberPickerHours.setValue(hours);
            numberPickerMinutes.setValue(minutes);
            numberPickerSeconds.setValue(seconds);
        }

        dialogBuilder.setPositiveButton(context.getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        String timerNameLocal = timerNameView.getText().toString().trim();
                        ContentValues values = new ContentValues();
                        values.put(MultiTimerEntry.COLUMN_TIMER_NAME, timerNameLocal);

                        numberPickerHours.clearFocus();
                        numberPickerMinutes.clearFocus();
                        numberPickerSeconds.clearFocus();

                        //Combine the time to make a time in milliseconds
                        long millis = TimeUnit.HOURS.toMillis(numberPickerHours.getValue());
                        millis += TimeUnit.MINUTES.toMillis(numberPickerMinutes.getValue());
                        millis += TimeUnit.SECONDS.toMillis(numberPickerSeconds.getValue());

                        values.put(MultiTimerEntry.COLUMN_TIMER_CLOCK, millis);

                        values.put(MultiTimerEntry.COLUMN_TIMER_SHOW, MultiTimerEntry.VALUE_TIMER_SHOW_TRUE);

                        if (!edit)
                        {
                            getActivity().getContentResolver().insert(MultiTimerEntry.CONTENT_URI_TIMER, values);
                        }
                        else
                        {
                            String selection = MultiTimerEntry._ID + MultiTimerProvider.getSqlJoker();
                            String[] selectionArgs = new String[] {String.valueOf(id)};
                            getActivity().getContentResolver().update(
                                    MultiTimerEntry.CONTENT_URI_TIMER,
                                    values,
                                    selection,
                                    selectionArgs);

                            //Update the timer name on the timer object
                            if (!timerNameLocal.equals(timer.getTimerName()))
                            {
                                timer.setTimerName(timerNameLocal);
                            }
                            //Update the timer clock on the timer object
                            timer.setTimerClock(millis);
                        }
                        MainActivity mainActivity = (MainActivity) getContext();
                        mainActivity.getLoaderManager().restartLoader(MainActivity.LOADER_ID, null, mainActivity);
                    }
                });

        dialogBuilder.setNegativeButton(context.getString(R.string.cancel_dialog),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });

        return dialogBuilder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void setSeconds(int newValue)
    {
        seconds = newValue;
    }

    private void setMinutes(int newValue)
    {
        minutes = newValue;
    }

    private void setHour(int newValue)
    {
        hours = newValue;
    }
}
