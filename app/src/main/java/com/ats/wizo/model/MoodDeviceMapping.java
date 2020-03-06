package com.ats.wizo.model;

public class MoodDeviceMapping {

    private int moodMapId;
    private int moodId;
    private String moodDevMac;
    private int moodDevType;
    private int moodOperation;
    private int moodDetailId;

    public int getMoodMapId() {
        return moodMapId;
    }

    public void setMoodMapId(int moodMapId) {
        this.moodMapId = moodMapId;
    }

    public int getMoodId() {
        return moodId;
    }

    public void setMoodId(int moodId) {
        this.moodId = moodId;
    }

    public String getMoodDevMac() {
        return moodDevMac;
    }

    public void setMoodDevMac(String moodDevMac) {
        this.moodDevMac = moodDevMac;
    }


    public int getMoodDevType() {
        return moodDevType;
    }

    public void setMoodDevType(int moodDevType) {
        this.moodDevType = moodDevType;
    }

    public int getMoodOperation() {
        return moodOperation;
    }

    public void setMoodOperation(int moodOperation) {
        this.moodOperation = moodOperation;
    }

    public int getMoodDetailId() {
        return moodDetailId;
    }

    public void setMoodDetailId(int moodDetailId) {
        this.moodDetailId = moodDetailId;
    }

    @Override
    public String toString() {
        return "MoodDeviceMapping{" +
                "moodMapId=" + moodMapId +
                ", moodId=" + moodId +
                ", moodDevMac='" + moodDevMac + '\'' +
                ", moodDevType=" + moodDevType +
                ", moodOperation=" + moodOperation +
                ", moodDetailId=" + moodDetailId +
                '}';
    }


}
