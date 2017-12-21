package com.chrismsolutions.chrismmultitimer;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.TimerTask;
import android.os.Handler;

import com.chrismsolutions.chrismmultitimer.data.MultiTimerContract.MultiTimerEntry;
import com.chrismsolutions.chrismmultitimer.data.MultiTimerProvider;

/**
 * Created by Christian Myrvold on 13.11.2017.
 */

public class MultiTimerArrayAdapter extends ArrayAdapter<MultiTimer>
{
    private final ArrayList<MultiTimer> multiTimerArrayList;
    private ArrayList<ViewHolder> viewHolders;
    private static int TIMER_DELAY = 0;
    private static int TIMER_PERIOD = 500;
    private Handler mHandler = new Handler();
    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            synchronized (viewHolders)
            {
                long shortestRemaining = Integer.MAX_VALUE;
                ArrayList<MultiTimer> timersDismiss = new ArrayList<>();
                MultiTimer timerNotify = null;

                for (ViewHolder viewHolder : viewHolders)
                {
                    if (viewHolder.timer.getStarted())
                    {
                        viewHolder.updateTime();

                        if (viewHolder.timer.getTimerClockRemaining() < shortestRemaining &&
                                !viewHolder.timer.isCountUp())
                        {
                            //We have a new timer with shortest time remaining
                            if (timerNotify != null)
                            {
                                //dismiss the old timer
                                timersDismiss.add(timerNotify);
                            }
                            timerNotify = viewHolder.timer;
                            shortestRemaining = timerNotify.getTimerClockRemaining();
                        }
                        else
                        {
                            timersDismiss.add(viewHolder.timer);
                        }
                    }
                }
                //Set ongoing notify on shortest timer, dismiss the rest
                for (MultiTimer timer : timersDismiss)
                {
                    timer.setShowOngoingNotification(false);
                }
                if (timerNotify != null)
                {
                    timerNotify.setShowOngoingNotification(true);
                }
            }
        }
    };

    public MultiTimerArrayAdapter(@NonNull Context context, ArrayList<MultiTimer> timers)
    {
        super(context, 0, timers);
        multiTimerArrayList = timers;
        viewHolders = new ArrayList<ViewHolder>();
        startUpdateTimers();
    }

    private void startUpdateTimers()
    {
        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(updateTimer);
            }
        }, TIMER_DELAY, TIMER_PERIOD);
    }

    @Nullable
    @Override
    public MultiTimer getItem(int position) {
        MultiTimer timer = null;
        timer = multiTimerArrayList != null ? multiTimerArrayList.get(position) : null;
        return timer;
    }

    @Override
    public int getCount() {
        return multiTimerArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listMultiTimerView = convertView;
        final ViewHolder viewHolder;
        TextView nameTextView;

        if (listMultiTimerView == null)
        {
            listMultiTimerView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);

            viewHolder = new ViewHolder();

            //Set the timer name
            nameTextView = (TextView) listMultiTimerView.findViewById(R.id.timer_name);
            viewHolder.nameText = nameTextView;

            //Set the timer clock
            TextView clockTextView = (TextView) listMultiTimerView.findViewById(R.id.timer_clock);
            viewHolder.clockText = clockTextView;

            //See and edit the multitimer info
            ImageView infoView = (ImageView) listMultiTimerView.findViewById(R.id.timer_info);
            infoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle arguments = new Bundle();
                    arguments.putSerializable(MultiTimerDialogInfoFragment.DIALOG_TIME_TIMER,
                            viewHolder.timer);

                    FragmentActivity fragmentActivity = (FragmentActivity)getContext();
                    MultiTimerDialogInfoFragment fragment = new MultiTimerDialogInfoFragment();
                    fragment.setArguments(arguments);
                    fragment.show(fragmentActivity.getSupportFragmentManager(), MultiTimerDialogInfoFragment.DIALOG_TAG);

                }
            });

            //Edit the multitimer object
            ImageView editView = (ImageView) listMultiTimerView.findViewById(R.id.timer_edit);
            editView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle arguments = new Bundle();
                    arguments.putBoolean(MultiTimerDialogFragment.DIALOG_EDIT, true);
                    arguments.putString(MultiTimerDialogFragment.DIALOG_TIMER_NAME, viewHolder.nameText.getText().toString());
                    arguments.putLong(MultiTimerDialogFragment.DIALOG_TIME_MILLIS, viewHolder.timer.getTimerClock());
                    arguments.putInt(MultiTimerDialogFragment.DIALOG_TIME_ID, viewHolder.timer.getId());
                    arguments.putSerializable(MultiTimerDialogFragment.DIALOG_TIME_TIMER, viewHolder.timer);

                    FragmentActivity fragmentActivity = (FragmentActivity)getContext();
                    MultiTimerDialogFragment fragment = new MultiTimerDialogFragment();
                    fragment.setArguments(arguments);
                    fragment.show(fragmentActivity.getSupportFragmentManager(), MultiTimerDialogFragment.DIALOG_TAG);

                }
            });
            viewHolder.editView = editView;

            //Delete the multitimer object
            final ImageView deleteView = (ImageView) listMultiTimerView.findViewById(R.id.timer_delete);
            deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popupMenu = new PopupMenu(getContext(), deleteView);
                    popupMenu.inflate(R.menu.delete_options);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            String timerName = viewHolder.timer.getTimerName();
                            String selection;
                            String[] selectionArgs;

                            selection = MultiTimerEntry._ID + MultiTimerProvider.getSqlJoker();
                            selectionArgs = new String[]{String.valueOf(viewHolder.timer.getId())};

                            String messageDelete = "";
                            String messageDeleted = "";
                            ContentValues values = null;

                            switch (menuItem.getItemId()) {
                                case R.id.delete_hide:
                                    messageDelete = getContext().getString(R.string.hide_timer, timerName);
                                    messageDeleted = getContext().getString(R.string.timer_hidden, timerName);
                                    values = new ContentValues();
                                    values.put(MultiTimerEntry.COLUMN_TIMER_SHOW, MultiTimerEntry.VALUE_TIMER_SHOW_FALSE);
                                    break;

                                case R.id.delete_real:
                                    messageDelete = getContext().getString(R.string.delete_timer, timerName);
                                    messageDeleted = getContext().getString(R.string.timer_deleted, timerName);
                                    break;

                                default:
                                    throw new IllegalArgumentException("Illegal menu ID: " + menuItem.getItemId());
                            }
                            DialogHelper.deleteConfirmationDialog(
                                    getContext(),
                                    messageDelete,
                                    messageDeleted,
                                    MultiTimerEntry.CONTENT_URI_TIMER,
                                    selection,
                                    selectionArgs,
                                    values);
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
            viewHolder.deleteView = deleteView;

            //Start/stop the countdown timer
            ImageView playPauseView = (ImageView) listMultiTimerView.findViewById(R.id.timer_play_pause);
            playPauseView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.setImagePlayPaused(false);
                }
            });
            viewHolder.playPauseView = playPauseView;

            //Reload the countdown timer
            ImageView reloadView = (ImageView) listMultiTimerView.findViewById(R.id.timer_replay);
            reloadView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.timer.pause();
                    viewHolder.timer.cancelOngoingNotification();
                    viewHolder.timer.setTimerClock(viewHolder.timer.getTimerClock());
                    viewHolder.setImagePlayPaused(true);
                    viewHolder.updateTime();
                }
            });
            viewHolder.reloadView = reloadView;

            synchronized (viewHolders)
            {
                viewHolders.add(viewHolder);
            }

            listMultiTimerView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) listMultiTimerView.getTag();
        }

        viewHolder.setData(getItem(position));

        return listMultiTimerView;
    }



    public ArrayList<MultiTimer> getMultiTimerArrayList() {
        return multiTimerArrayList;
    }

    private static class ViewHolder
    {
        public TextView nameText, clockText;
        public ImageView editView, deleteView, playPauseView, reloadView;
        public MultiTimer timer;

        public void updateTime()
        {
            nameText.setText(timer.getTimerName());
            clockText.setText(timer.toString());
            setImagePlayPaused(true);
        }


        public void setData(MultiTimer mTimer)
        {
            timer = mTimer;
            updateTime();
        }


        public void setImagePlayPaused(boolean onlyChangeImage)
        {
            boolean started = timer.getStarted();

            if (!onlyChangeImage)
            {
                timer.setStarted(!started);
            }

            if (started && timer.getFinished())
            {
                if (!onlyChangeImage)
                {
                    //Timer is done, stop the alarm and set the image back to PLAY and hide RELOAD
                    playPauseView.setImageResource(R.mipmap.ic_play_circle_outline_black_48dp);
                    reloadView.setVisibility(View.GONE);
                    timer.setFinished(false);
                    timer.stopRingtone();
                }
                else
                {
                    //Alarm is playing, show the ALARM image and hide the RELOAD image
                    playPauseView.setImageResource(R.mipmap.ic_alarm_off_black_48dp);
                    reloadView.setVisibility(View.GONE);
                }
            }
            else if (started)
            {
                if (!onlyChangeImage)
                {
                    //PAUSE clicked, show the PLAY and RELOAD image and pause the timer
                    playPauseView.setImageResource(R.mipmap.ic_play_circle_outline_black_48dp);
                    reloadView.setVisibility(View.VISIBLE);
                    timer.pause();
                }
                else
                {
                    //Timer is playing, show the PAUSE image and hide the RELOAD image
                    playPauseView.setImageResource(R.mipmap.ic_pause_circle_outline_black_48dp);
                    reloadView.setVisibility(View.GONE);
                }
            }
            else
            {
                if (!onlyChangeImage)
                {
                    //PLAY is clicked, start the timer and show the PAUSE and RELOAD image
                    playPauseView.setImageResource(R.mipmap.ic_pause_circle_outline_black_48dp);
                    reloadView.setVisibility(View.GONE);
                    timer.start();
                }
                else
                {
                    //Timer is idle, show the PLAY image and hide the REPLAY image
                    playPauseView.setImageResource(R.mipmap.ic_play_circle_outline_black_48dp);
                    reloadView.setVisibility(View.GONE);
                }
            }
        }
    }

}
