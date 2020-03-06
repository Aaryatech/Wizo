package com.ats.wizo.model;

public class MoodHeader {

    private int moodHeaderId;


    private int userId;

    private String moodName;


    public int getMoodHeaderId() {
        return moodHeaderId;
    }

    public void setMoodHeaderId(int moodHeaderId) {
        this.moodHeaderId = moodHeaderId;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMoodName() {
        return moodName;
    }

    public void setMoodName(String moodName) {
        this.moodName = moodName;
    }


    @Override
    public String toString() {
        return "MoodHeader{" +
                "moodHeaderId=" + moodHeaderId +

                ", userId=" + userId +
                ", moodName='" + moodName + '\'' +
                '}';
    }
}
