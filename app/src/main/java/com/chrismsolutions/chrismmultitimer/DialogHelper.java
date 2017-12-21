package com.chrismsolutions.chrismmultitimer;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.widget.Toast;


/**
 * Created by Christian Myrvold on 30.10.2017.
 */

public class DialogHelper
{
    //Show a dialog to have the user confirm they actually wish to delete the folder. If OK, call
     // the delete method in the provider.

    /**
     *
     * @param context
     * @param messageDelete The message shown to make sure the user wishes to delete
     * @param messageDeleted The message shown to confirm what has been deleted
     * @param contentUri
     * @param selection
     * @param selectionArgs
     */
    public static void deleteConfirmationDialog(final Context context,
                                                String messageDelete,
                                                final String messageDeleted,
                                                final Uri contentUri,
                                                final String selection,
                                                final String[] selectionArgs,
                                                final ContentValues values)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage(messageDelete);

        dialogBuilder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int result;

                if (values == null)
                {
                    result = context.getContentResolver().delete(
                            contentUri, selection, selectionArgs);
                }
                else
                {
                    result = context.getContentResolver().update(contentUri, values, selection, selectionArgs);
                }

                MainActivity mainActivity = (MainActivity) context;
                mainActivity.getLoaderManager().restartLoader(MainActivity.LOADER_ID, null, mainActivity);

                if (result > 0)
                {
                    Toast.makeText(context, messageDeleted, Toast.LENGTH_SHORT).show();

                    //remove timer object from arraylist
                    mainActivity.setDeleted(Integer.parseInt(selectionArgs[0]));
                }
            }
        });

        dialogBuilder.setNegativeButton(context.getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null)
                {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
}
