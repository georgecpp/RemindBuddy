package com.steelparrot.remindbuddy;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

public class CompleteTaskReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        UUID taskId = UUID.fromString(intent.getSerializableExtra("TaskId").toString());
        Task mTask = TaskHandler.get(context).getTask(taskId);
        mTask.setCompleted(true);
        TaskHandler.get(context).updateTask(mTask);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(intent.getIntExtra("NotificationId",0));
    }
}
