package com.chrismsolutions.chrismmultitimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.widget.ExpandableListView;

import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christian Myrvold on 13.11.2017.
 */

public class MultiTimer implements Serializable
{
    private static final int SECONDS_DIVIDER = 120;
    private static final int SECONDS_OVER_MINUTE = 59;
    private String timerName;
    private String timerInfo;
    private long timerClock;
    private long timerClockRemaining;
    private Context mContext;
    private boolean started;
    private boolean finished;
    private boolean countUp;
    private int id;
    private boolean deleted = false;
    private boolean showOngoingNotification = false;

    public static int TIME_HOUR = 0;
    public static int TIME_MINUTE = 1;
    public static int TIME_SECOND = 2;

    private CountDownTimer countDownTimer;
    private static long SECOND_IN_MILLISECONDS = 1000;
    private static long COUNTDOWN_INTERVAL = 1000;
    private static final int ALARM_DELAY_MILLISECONDS = 30 * 1000;
    private static String CHANNEL_ID = "Chrism MultiTimer";
    private static int NOTIFICATION_ID = 5500;
    private static int NOTIFICATION_ID_ONGOING = 5600;
    private NotificationManager notificationManager;

    Handler mHandler = new Handler(Looper.getMainLooper());

    MultiTimer(String name,
               String info,
               long clockInMilliseconds,
               Context context,
               int id)
    {
        timerName = name;
        timerInfo = info;
        timerClock = clockInMilliseconds;
        timerClockRemaining = timerClock;
        mContext = context;
        started = false;
        finished = false;
        this.id = id;

        countUp = timerClock == 0;

        notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public String toString()
    {
        int hours, minutes, seconds;
        String text;

        int[] time = convertMillisToTime(timerClockRemaining);

        hours = time[TIME_HOUR];
        minutes = time[TIME_MINUTE];
        seconds = time[TIME_SECOND];

        if (seconds > SECONDS_OVER_MINUTE)
        {
            seconds /= SECONDS_DIVIDER;
        }

        text = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);

        return text;
    }

    public static int[] convertMillisToTime(long millis)
    {
        int hours = (int)TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        int minutes = (int)TimeUnit.MILLISECONDS.toMinutes(millis); //- (int)TimeUnit.HOURS.toMinutes(hours);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        int seconds = (int)TimeUnit.MILLISECONDS.toSeconds(millis); //- (int)TimeUnit.MINUTES.toSeconds(minutes);
        millis -= TimeUnit.SECONDS.toMillis(seconds);

        return new int[]{hours, minutes, seconds};
    }

    public void setTimerClock(long timerClock)
    {
        this.timerClock = timerClock;

        if (!started)
        {
            timerClockRemaining = timerClock;
        }
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public long getTimerClock() {
        return timerClock;
    }

    public String getTimerName() {
        return timerName;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean getStarted()
    {
        return started;
    }

    public boolean getFinished()
    {
        return finished;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    String getTimerInfo() {
        return timerInfo;
    }

    void setTimerInfo(String timerInfo) {
        this.timerInfo = timerInfo;
    }

    public boolean isShowOngoingNotification() {
        return showOngoingNotification;
    }

    void setShowOngoingNotification(boolean showOngoingNotification) {
        this.showOngoingNotification = showOngoingNotification;
    }

    long getTimerClockRemaining()
    {
        return timerClockRemaining;
    }

    public boolean isCountUp()
    {
        return countUp;
    }

    void start()
    {
        started = true;
        finished = false;

        try
        {
            mHandler.removeCallbacks(countdownTask);
            mHandler.post(countdownTask);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private Runnable countdownTask = new Runnable() {
        @Override
        public void run() {
            long countdownTo = countUp ? Integer.MAX_VALUE : timerClockRemaining;
            countDownTimer = new CountDownTimer(countdownTo, COUNTDOWN_INTERVAL) {
                @Override
                public void onTick(long l) {
                    if (!countUp) {
                        timerClockRemaining = l;
                    }
                    else
                    {
                        timerClockRemaining += SECOND_IN_MILLISECONDS;
                    }
                    showOngoingNotification();
                }

                @Override
                public void onFinish() {
                    timerClockRemaining = timerClock;
                    finished = true;
                    mHandler.removeCallbacks(countdownTask);
                    showNotification();
                }
            }.start();
        }
    };

    private void showOngoingNotification()
    {
        if (showOngoingNotification && timerClockRemaining != 0)
        {
            Notification.Builder builder = new Notification.Builder(mContext)
                    .setSmallIcon(R.mipmap.multitimer_512_official)
                    .setContentTitle(mContext.getString(R.string.app_name))
                    .setContentText(mContext.getString(R.string.ongoing_notification_text, getTimerName(), toString()));

            builder.setOngoing(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                builder.setPriority(Notification.PRIORITY_MAX);
            }

            Intent resultIntent = new Intent(mContext, MainActivity.class);
            //Intent[] intents = new Intent[]{resultIntent};
            PendingIntent resultPendingIntent = PendingIntent.getActivity(
                    mContext,
                    0,
                    resultIntent,//intents,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            builder.setContentIntent(resultPendingIntent);

            notificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            {
                notificationManager.notify(NOTIFICATION_ID_ONGOING, builder.build());
            }
            else
            {
                notificationManager.notify();
            }
        }
        else if (timerClockRemaining == 0)
        {
            notificationManager.cancel(NOTIFICATION_ID_ONGOING);
        }
    }

    public void cancelOngoingNotification()
    {
        showOngoingNotification = false;
        notificationManager.cancel(NOTIFICATION_ID_ONGOING);
    }

    private void showNotification()
    {
        Notification.Builder builder = new Notification.Builder(mContext)
                .setSmallIcon(R.mipmap.multitimer_512_official)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.timer_done_notification, getTimerName()));

        Intent resultIntent = new Intent(mContext, MainActivity.class);
        Intent[] intents = new Intent[]{resultIntent};
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                mContext,
                0,
                resultIntent,//intents,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);
        builder.setAutoCancel(true);
        builder.setOngoing(true);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), Notification.FLAG_INSISTENT);

        final NotificationManager notificationManagerLocal =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManagerLocal != null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            {
                notificationManagerLocal.notify(NOTIFICATION_ID, builder.build());
            }
            else
            {
                notificationManagerLocal.notify();
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notificationManagerLocal.cancel(NOTIFICATION_ID);
                    finished = false;
                    cancelOngoingNotification();
                }
            }, ALARM_DELAY_MILLISECONDS);
        }
    }

    public void stopRingtone()
    {
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        cancelOngoingNotification();
    }

    public void pause()
    {
        started = false;
        mHandler.removeCallbacks(countdownTask);
        countDownTimer.cancel();
    }
}
