package com.steelparrot.remindbuddy;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
//import android.support.v4.app.NotificationCompat;

import androidx.core.app.NotificationCompat;


public class NotificationHelper extends ContextWrapper {
    public static final String channelID = "channelID";
    public static final String channelName = "Channel Name";

    private NotificationManager mManager;
    private String mTaskTitle;

    public NotificationHelper(Context base, String taskTitle) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
        mTaskTitle = taskTitle;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;
    }

    public NotificationCompat.Builder getChannelNotification() {

        Intent taskListActivityIntent = new Intent(this, TaskListActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, taskListActivityIntent, 0);
        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle(mTaskTitle)
                .setContentText("Do not forget to complete the task!")
                .setColor(Color.GREEN)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher, "Complete task", contentIntent)
                .setSmallIcon(R.drawable.ic_baseline_android_24);
    }
}