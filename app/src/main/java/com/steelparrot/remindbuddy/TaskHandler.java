package com.steelparrot.remindbuddy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.style.TextAppearanceSpan;

import com.steelparrot.remindbuddy.database.TaskBaseHelper;
import com.steelparrot.remindbuddy.database.TaskCursorWrapper;
import com.steelparrot.remindbuddy.database.TaskDbSchema.TaskTable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Singleton class to generate and handle Task object.
public class TaskHandler {
    private static TaskHandler sTaskHandler; // singleton.

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public void addTask(Task task) {
        ContentValues values = getContentValues(task);
        mDatabase.insert(TaskTable.NAME, null, values);
    }
    public void updateTask(Task task) {
        String uuidString = task.getID().toString();
        ContentValues values = getContentValues(task);

        mDatabase.update(TaskTable.NAME, values, TaskTable.Cols.UUID + " = ?", new String[] {uuidString});
    }
    public void deleteTask(Task task) {
        String uuidString = task.getID().toString();
        mDatabase.delete(TaskTable.NAME, TaskTable.Cols.UUID + " = ?", new String[] {uuidString});
    }

    public Task getTask(UUID id) {
        Task taskToReturn = null;
        TaskCursorWrapper cursor = queryTasks(TaskTable.Cols.UUID + " =?", new String[]{id.toString()});
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            taskToReturn = cursor.getTask();

        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return taskToReturn;
    }

    public List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();

        TaskCursorWrapper cursor = queryTasks(null, null);

        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                tasks.add(cursor.getTask());
                cursor.moveToNext();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return tasks;
    }

    public List<Task> getTasksForToday(String currDate) {
        List<Task> tasks = new ArrayList<>();

        TaskCursorWrapper cursorWrapper = queryTasks(TaskTable.Cols.DATE + " =?", new String[] {currDate});
        try {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                tasks.add(cursorWrapper.getTask());
                cursorWrapper.moveToNext();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            cursorWrapper.close();
        }

        return tasks;
    }



    private TaskHandler(Context context) {
        mContext = context;
        mDatabase = new TaskBaseHelper(mContext).getWritableDatabase();
    }

    public static TaskHandler get(Context context) {
        if(sTaskHandler==null) {
            sTaskHandler = new TaskHandler(context);
        }
        return sTaskHandler;
    }

    // getter of row with task's specific properties.
    private static ContentValues getContentValues(Task task) {
        ContentValues values = new ContentValues();
        values.put(TaskTable.Cols.UUID, task.getID().toString());
        values.put(TaskTable.Cols.TITLE,task.getTitle());
        values.put(TaskTable.Cols.DESCRIPTION,task.getDescription());
        values.put(TaskTable.Cols.COMPLETED, task.isCompleted()? 1 : 0);
        values.put(TaskTable.Cols.DATE, task.getDate());
        values.put(TaskTable.Cols.TIME,task.getTime());

        return values;
    }

    private TaskCursorWrapper queryTasks(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                TaskTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                TaskTable.Cols.TIME
        );

        return new TaskCursorWrapper(cursor);
    }

}
