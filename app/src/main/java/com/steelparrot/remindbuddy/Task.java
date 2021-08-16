package com.steelparrot.remindbuddy;

import java.util.Date;
import java.util.UUID;

public class Task {
    private UUID mID;
    private String mTitle;
    private String mDescription;
    private boolean mCompleted;
    private String mDate;
    private String mTime;
    private int mNotificationIdAssigned;

    public Task(UUID id, String dateTo, String timeTo)
    {
        mID = id;
        mDate = dateTo;
        mTime = timeTo;
    }

    public UUID getID() {
        return mID;
    }

    public void setID(UUID ID) {
        mID = ID;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public boolean isCompleted() {
        return mCompleted;
    }

    public void setCompleted(boolean completed) {
        mCompleted = completed;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public int getNotificationIdAssigned() {
        return mNotificationIdAssigned;
    }

    public void setNotificationIdAssigned(int notificationIdAssigned) {
        mNotificationIdAssigned = notificationIdAssigned;
    }
}
