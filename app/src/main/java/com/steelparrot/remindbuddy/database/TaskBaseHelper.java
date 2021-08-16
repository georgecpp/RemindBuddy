package com.steelparrot.remindbuddy.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.steelparrot.remindbuddy.database.TaskDbSchema.TaskTable;

public class TaskBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "taskBase.db";

    public TaskBaseHelper(Context context) {
        super(context,DATABASE_NAME,null,VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ TaskTable.NAME + " (" + " _id integer primary key autoincrement, " +
                TaskTable.Cols.UUID+", "+
                TaskTable.Cols.TITLE+", "+
                TaskTable.Cols.DESCRIPTION+", "+
                TaskTable.Cols.COMPLETED+", "+
                TaskTable.Cols.DATE+", "+
                TaskTable.Cols.TIME+", "+
                TaskTable.Cols.NOTIFICATIONID+
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            //
    }
}
