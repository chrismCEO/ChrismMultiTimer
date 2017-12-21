package com.chrismsolutions.chrismmultitimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.chrismsolutions.chrismmultitimer.data.MultiTimerContract.MultiTimerEntry;
import com.chrismsolutions.chrismmultitimer.data.MultiTimerProvider;

import java.util.ArrayList;

/**
 * Created by Christian Myrvold on 22.11.2017.
 */

public class MultiTimerHiddenDialogFragment extends DialogFragment
{
    public final static String DIALOG_TAG = "MultiTimerHiddenDialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Context context = getActivity();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        final MultiTimerHiddenCursorAdapter adapter = new MultiTimerHiddenCursorAdapter(context, getHiddenTimersFromDB(context));

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.content_hidden_timers, null);
        final ListView hiddenTimers = dialogView.findViewById(R.id.list_hidden_timers);
        hiddenTimers.setAdapter(adapter);

        dialogBuilder.setTitle(context.getString(R.string.hidden_timers_dialog_title));

        TextView emptyViewHidden = dialogView.findViewById(R.id.empty_view_hidden_timers);
        hiddenTimers.setEmptyView(emptyViewHidden);

        dialogBuilder.setView(dialogView);

        dialogBuilder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                ArrayList<String> ids = new ArrayList<String>();

                SparseBooleanArray checkStates = adapter.getCheckStates();
                for (int j = 0; j < checkStates.size(); j++)
                {
                    int id = checkStates.keyAt(j);

                    if (checkStates.get(id))
                    {
                        ids.add(String.valueOf(id));
                    }
                }

                String selection = MultiTimerEntry._ID + MultiTimerProvider.getSqlJoker();
                String[] selectionArgs = new String[ids.size()];
                for (int k = 0; k < ids.size(); k++)
                {
                    selectionArgs[k] = ids.get(k);
                }

                ContentValues values = new ContentValues();
                values.put(MultiTimerEntry.COLUMN_TIMER_SHOW, MultiTimerEntry.VALUE_TIMER_SHOW_TRUE);

                getActivity().getContentResolver().update(
                        MultiTimerEntry.CONTENT_URI_TIMER,
                        values,
                        selection,
                        selectionArgs);

                //Reload MainActivity
                MainActivity mainActivity = (MainActivity) getContext();
                mainActivity.getLoaderManager().restartLoader(MainActivity.LOADER_ID, null, mainActivity);

            }
        });

        dialogBuilder.setNegativeButton(context.getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });

        return dialogBuilder.create();
    }

    private Cursor getHiddenTimersFromDB(Context context)
    {
        String[] projection = {
                MultiTimerEntry._ID,
                MultiTimerEntry.COLUMN_TIMER_NAME,
                MultiTimerEntry.COLUMN_TIMER_INFO,
                MultiTimerEntry.COLUMN_TIMER_CLOCK,
                MultiTimerEntry.COLUMN_TIMER_SHOW
        };

        String selection = MultiTimerEntry.COLUMN_TIMER_SHOW + MultiTimerProvider.getSqlJoker();
        String[] selectionArgs = new String[]{String.valueOf(MultiTimerEntry.VALUE_TIMER_SHOW_FALSE)};
        String sortOrder = MultiTimerEntry.COLUMN_TIMER_NAME + " ASC";

        return context.getContentResolver().query(
                MultiTimerEntry.CONTENT_URI_TIMER,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );

    }
}
