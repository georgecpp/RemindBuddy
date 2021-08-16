package com.steelparrot.remindbuddy.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.steelparrot.remindbuddy.Task;
import com.steelparrot.remindbuddy.database.TaskDbSchema.TaskTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class TaskCursorWrapper extends CursorWrapper {

    public TaskCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Task getTask() throws ParseException {
        String uuidString = getString(getColumnIndex(TaskTable.Cols.UUID));
        String title = getString(getColumnIndex(TaskTable.Cols.TITLE));
        String description = getString(getColumnIndex(TaskTable.Cols.DESCRIPTION));
        int isCompleted = getInt(getColumnIndex(TaskTable.Cols.COMPLETED));
        String date = getString(getColumnIndex(TaskTable.Cols.DATE));
        String time = getString(getColumnIndex(TaskTable.Cols.TIME));
        int notificationId = getInt(getColumnIndex(TaskTable.Cols.NOTIFICATIONID));

//        SimpleDateFormat formatDDMMYYYY = new SimpleDateFormat("dd/MM/yyyy");
//        SimpleDateFormat formatHHMMSS = new SimpleDateFormat("HH:mm:ss");

//        Date dateTask = formatDDMMYYYY.parse(date);
//        Date timeOfTheDayTask = formatHHMMSS.parse(time);

//        int notificationId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        Task task = new Task(UUID.fromString(uuidString),date,time);
        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(isCompleted!=0);
        task.setNotificationIdAssigned(notificationId);

        return task;
    }
}
