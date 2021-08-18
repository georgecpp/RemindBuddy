package com.steelparrot.remindbuddy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;

import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AlertReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

            int notificationId = intent.getIntExtra("NotificationId",0);
            NotificationHelper notificationHelper = new NotificationHelper(context, intent.getStringExtra("TaskTitle"), intent.getSerializableExtra("TaskId").toString(), notificationId);
            NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
            notificationHelper.getManager().notify(notificationId,nb.build());


            WorkManager workManager = WorkManager.getInstance(context);
            Data.Builder dataBuilder = new Data.Builder();
            dataBuilder.putString("TaskTitle", intent.getStringExtra("TaskTitle"));
            dataBuilder.putInt("NotificationId", notificationId);
            workManager.enqueue(new OneTimeWorkRequest.Builder(SpeakWorker.class).setInputData(dataBuilder.build()).build());

    }
}
