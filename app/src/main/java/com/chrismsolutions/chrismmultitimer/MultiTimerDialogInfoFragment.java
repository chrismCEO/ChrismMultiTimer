package com.chrismsolutions.chrismmultitimer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.chrismsolutions.chrismmultitimer.data.MultiTimerContract.MultiTimerEntry;
import com.chrismsolutions.chrismmultitimer.data.MultiTimerProvider;

/**
 * Created by Christian Myrvold on 28.11.2017.
 */

public class MultiTimerDialogInfoFragment extends DialogFragment
{
    public static final String DIALOG_INFO = "DIALOG_INFO";
    public static final String DIALOG_ID = "DIALOG_ID";
    public static final String DIALOG_TIME_TIMER = "DIALOG_TIME_TIMER";
    public static final String DIALOG_TAG = "DIALOG_TAG";

    public MultiTimerDialogInfoFragment()
    {
        super();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Context context = getActivity();
        Bundle arguments = getArguments();
        final EditText infoEdit;
        String info;
        final int id;
        final MultiTimer timer;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_timer_info, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle(R.string.information_title);

        infoEdit = (EditText) dialogView.findViewById(R.id.dialog_info_edit);

        if (arguments != null)
        {
            timer = (MultiTimer) arguments.getSerializable(DIALOG_TIME_TIMER);
            info = timer.getTimerInfo();
            id = timer.getId();

            if (!TextUtils.isEmpty(info))
            {
                infoEdit.setText(info);
            }
        }
        else
        {
            timer = null;
            id = 0;
        }

        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String infoLocal = infoEdit.getText().toString();

                ContentValues values = new ContentValues();
                values.put(MultiTimerEntry.COLUMN_TIMER_INFO, infoLocal);

                String selection = MultiTimerEntry._ID + MultiTimerProvider.getSqlJoker();
                String[] selectionArgs = new String[] {String.valueOf(id)};
                int result = getActivity().getContentResolver().update(
                        MultiTimerEntry.CONTENT_URI_TIMER,
                        values,
                        selection,
                        selectionArgs);

                timer.setTimerInfo(infoLocal);
            }
        });

        dialogBuilder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });

        return dialogBuilder.create();
    }
}
