package com.steelparrot.remindbuddy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationCompat;

import java.util.Date;
import java.util.Random;

public class AlertReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
            NotificationHelper notificationHelper = new NotificationHelper(context, intent.getStringExtra("TaskTitle"));
            NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
            int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            m += new Random().nextInt(100) + 1;
            notificationHelper.getManager().notify(m,nb.build());
    }
}
