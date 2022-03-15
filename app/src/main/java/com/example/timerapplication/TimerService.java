package com.example.timerapplication;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.logging.Logger;

public class TimerService extends Service {

    CountDownTimer timer;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground();
        }
        if(intent.getExtras() != null){
            int mil = intent.getExtras().getInt("milliseconds");
            setupTimer(mil);
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    private void setupTimer(int milliseconds) {
        timer = new CountDownTimer(milliseconds,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String progress = getMinutesAndSecondsStringFromMilliSeconds(millisUntilFinished);
                setProgressIntent(progress);
                updateNotification(progress);
            }

            @Override
            public void onFinish() {
                notificationManager.cancel(5000);
                sendFinishedBroadCast();
                stopSelf();
            }
        };
        timer.start();
    }

    private void sendFinishedBroadCast() {
        Intent progressIntent = new Intent();

        progressIntent.setAction("timerProgressEnded");


        sendBroadcast(progressIntent);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    void startForeground(){
        NotificationChannel channel = new NotificationChannel(
                "TIMER_PROGRESS",
                "Progress Noification",
                NotificationManager.IMPORTANCE_LOW);
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);

        builder = new NotificationCompat.Builder(getApplicationContext(),"TIMER_PROGRESS");

        builder.setContentTitle("Progress")
                .setSmallIcon(R.drawable.ic_launcher_background);

        startForeground(5000,builder.build());
    }
    private void updateNotification(String progress) {
        builder.setContentText(progress);
        notificationManager.notify(5000,builder.build());
    }

    private void setProgressIntent(String progress) {
        Intent progressIntent = new Intent();

        progressIntent.setAction("timerProgress");

        progressIntent.putExtra("progress",progress);

        sendBroadcast(progressIntent);
    }

    @Override
    public boolean stopService(Intent name) {
        timer.cancel();
        return super.stopService(name);
    }

    private String getMinutesAndSecondsStringFromMilliSeconds(long millisUntilFinished) {
        long seconds = millisUntilFinished / 1000;
        long minutes = seconds / 60;
        long secondsRemaining = seconds%60;
        String time = "";
        String minuteString = String.valueOf(minutes);
        if(minutes < 10)
            minuteString = "0" + minuteString;
        String secondsString = String.valueOf(secondsRemaining);
        if(secondsRemaining < 10)
            secondsString = "0" + secondsString;
        time = minuteString + ":" + secondsString;
        return time;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
