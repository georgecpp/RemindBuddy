package com.steelparrot.remindbuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AlertRebootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED") || intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON")) {

            // set the alarms again.
            // iterate through all tasks for today's date and turn on alarms on TimeSet.
            List<Task> mTasks = TaskHandler.get(context).getTasksForToday(TaskListFragment.getCurrentDate());
            for(int i=0;i<mTasks.size();i++) {
                try {
                    // obtain calendar with hour and minute set from task.
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
                    calendar.setTime(sdf.parse(mTasks.get(i).getDate()));

                    String taskTime_AM_PM = mTasks.get(i).getTime();
                    if(taskTime_AM_PM.contains("PM")) {
                        calendar.set(Calendar.AM_PM, Calendar.PM);
                    }
                    else {
                        calendar.set(Calendar.AM_PM, Calendar.AM);
                    }
                    String[] hour_minutes = taskTime_AM_PM.split(":");
                    calendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(hour_minutes[0]));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(hour_minutes[1].split(" ")[0]));

                    if(calendar.getTimeInMillis() >= Calendar.getInstance(Locale.getDefault()).getTimeInMillis()) {
                        startAlarm(context,calendar,mTasks.get(i).getTitle());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void startAlarm(Context context, Calendar calendar, String taskTitle) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlertReceiver.class);
            intent.putExtra("TaskTitle", taskTitle);
            int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            m += new Random().nextInt(100) + 1;
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, m, intent, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
            else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
            }
    }
}
