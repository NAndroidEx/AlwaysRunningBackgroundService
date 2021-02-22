package com.nandroidex.backgroundservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {
    public int counter = 0;
    boolean restart = true;
    private RemoteViews notificationLayout;
    private Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationLayout = new RemoteViews(getPackageName(), R.layout.layout_service_notification);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "service";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContent(notificationLayout)
                .setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(
                        PendingIntent.getActivity(
                                this, 0,
                                new Intent(BackgroundService.this, MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT
                        )
                )
                .build();
        startForeground(2, notification);
        restart = true;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stoptimertask();

        if (restart) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("restartservice");
            broadcastIntent.setClass(this, RestartBroadcastReceiver.class);
            this.sendBroadcast(broadcastIntent);
        }
    }


    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
//        timer = new Timer();
//        timerTask = new TimerTask() {
//            public void run() {
//                Log.i("Count", "=========  " + (counter++));
//            }
//        };
//        timer.schedule(timerTask, 1000, 1000); //
        new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i("Count", "=========  " + (millisUntilFinished / 1000));
                if (notification != null) {
                    notificationLayout.setTextViewText(R.id.tvDetail, "Task will be done in " + (millisUntilFinished / 1000) + " second(s)");
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(2, notification);
                }
            }

            @Override
            public void onFinish() {
                showNotification();
            }
        }.start();
    }

    private void showNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("timer_channel", "Timer Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Channel Description");
            notificationChannel.enableLights(true);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationLayout.setTextViewText(R.id.tvDetail, "10 Seconds Completed");

        Notification builder = new NotificationCompat.Builder(this, "timer_channel")
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContent(notificationLayout)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("10 Seconds Completed")
                .setAutoCancel(true)
                .setContentIntent(
                        PendingIntent.getActivity(
                                this, 0,
                                new Intent(BackgroundService.this, MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT
                        )
                )
                .build();
        notificationManager.notify(1, builder);

        restart = false;
        stopSelf();
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
