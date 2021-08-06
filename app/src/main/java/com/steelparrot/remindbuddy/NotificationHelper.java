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
import androidx.core.app.TaskStackBuilder;

import java.util.UUID;


public class NotificationHelper extends ContextWrapper {
    public static final String channelID = "channelID";
    public static final String channelName = "Channel Name";

    private NotificationManager mManager;
    private String mTaskTitle;
    private UUID mTaskId;
    private int mNotificationId;

    public NotificationHelper(Context base, String taskTitle, String taskId, int NotificationId) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
        mTaskTitle = taskTitle;
        mTaskId = UUID.fromString(taskId);
        mNotificationId = NotificationId;
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
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(taskListActivityIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, taskListActivityIntent, 0);



        Intent taskCompletedIntent = new Intent(this, CompleteTaskReceiver.class);
        taskCompletedIntent.putExtra("TaskId", mTaskId);
        taskCompletedIntent.putExtra("NotificationId",mNotificationId);
        PendingIntent actionIntent = PendingIntent.getBroadcast(this, mNotificationId, taskCompletedIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle(mTaskTitle)
                .setContentText("Do not forget to complete the task!")
                .setColor(Color.GREEN)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher, "Complete task", actionIntent)
                .setSmallIcon(R.drawable.ic_baseline_android_24);
    }
}