package com.steelparrot.remindbuddy.database;

public class TaskDbSchema {
    public static final class TaskTable{
        public static final String NAME="tasks";

        public static final class Cols {
            public static final String UUID="uuid"; // TaskTable.Cols.UUID;
            public static final String TITLE="title";
            public static final String DESCRIPTION="description";
            public static final String COMPLETED="completed";
            public static final String DATE="date";
            public static final String TIME="time";
        }
    }
}
