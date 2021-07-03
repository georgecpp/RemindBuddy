package com.steelparrot.remindbuddy;

import java.util.Date;
import java.util.UUID;

public class Task {
    private UUID mID;
    private String mTitle;
    private String mDescription;
    private boolean mCompleted;
    private Date mDate;
    private Date mTime;

    public Task(UUID id, Date dateTo, Date timeTo)
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

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public Date getTime() {
        return mTime;
    }

    public void setTime(Date time) {
        mTime = time;
    }
}
