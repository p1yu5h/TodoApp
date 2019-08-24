package com.piyushsatija.todo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class TodoItem implements Parcelable {
    @ServerTimestamp
    Date date;
    private String taskName;
    private String taskTime;
    private boolean completed;

    public TodoItem(String taskName, String taskTime) {
        this.taskName = taskName;
        this.taskTime = taskTime;
    }

    public TodoItem() {
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(String taskTime) {
        this.taskTime = taskTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    private TodoItem(Parcel in) {
        this.taskName = in.readString();
        this.taskTime = in.readString();
        this.completed = (boolean) in.readValue(getClass().getClassLoader());
        this.date = (Date) in.readValue(getClass().getClassLoader());
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public TodoItem createFromParcel(Parcel in) {
            return new TodoItem(in);
        }

        public TodoItem[] newArray(int size) {
            return new TodoItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.taskName);
        parcel.writeString(this.taskTime);
        parcel.writeValue(this.completed);
        parcel.writeValue(this.date);
    }
}
