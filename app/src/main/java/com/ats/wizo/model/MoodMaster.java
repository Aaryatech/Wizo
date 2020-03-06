package com.ats.wizo.model;

public class MoodMaster {

   private int moodId;
   private String  moodName;
    private int moodStatus;

    public int getMoodId() {
        return moodId;
    }

    public void setMoodId(int moodId) {
        this.moodId = moodId;
    }

    public String getMoodName() {
        return moodName;
    }

    public void setMoodName(String moodName) {
        this.moodName = moodName;
    }

    public int getMoodStatus() {
        return moodStatus;
    }

    public void setMoodStatus(int moodStatus) {
        this.moodStatus = moodStatus;
    }


    @Override
    public String toString() {
        return "MoodMaster{" +
                "moodId=" + moodId +
                ", moodName='" + moodName + '\'' +
                ", moodStatus=" + moodStatus +
                '}';
    }
}
