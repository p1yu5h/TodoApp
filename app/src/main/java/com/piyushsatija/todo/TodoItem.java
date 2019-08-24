package com.piyushsatija.todo;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class TodoItem {
    @ServerTimestamp
    Date date;
    private String taskName;
    private String taskTime;
    private boolean isCompleted;

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
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
