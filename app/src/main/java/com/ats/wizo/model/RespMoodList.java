package com.ats.wizo.model;

import java.util.List;

public class RespMoodList {


    private List<MoodsList> moodsList = null;
    private Boolean error;
    private String message;


    public List<MoodsList> getMoodsList() {
        return moodsList;
    }

    public void setMoodsList(List<MoodsList> moodsList) {
        this.moodsList = moodsList;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "RespMoodList{" +
                "moodsList=" + moodsList +
                ", error=" + error +
                ", message='" + message + '\'' +
                '}';
    }
}
