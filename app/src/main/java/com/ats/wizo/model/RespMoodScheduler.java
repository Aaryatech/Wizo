package com.ats.wizo.model;

public class RespMoodScheduler {

    private int scheId;
    private int moodId;
    private int userId;
    private int operation;
    private int day;
    private String time;
    private int schStatus;

    public int getScheId() {
        return scheId;
    }

    public void setScheId(int scheId) {
        this.scheId = scheId;
    }

    public int getMoodId() {
        return moodId;
    }

    public void setMoodId(int moodId) {
        this.moodId = moodId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getSchStatus() {
        return schStatus;
    }

    public void setSchStatus(int schStatus) {
        this.schStatus = schStatus;
    }

    @Override
    public String toString() {
        return "RespMoodScheduler{" +
                "scheId=" + scheId +
                ", moodId=" + moodId +
                ", userId=" + userId +
                ", operation=" + operation +
                ", day=" + day +
                ", time='" + time + '\'' +
                ", schStatus=" + schStatus +
                '}';
    }
}
