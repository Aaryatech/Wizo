package com.ats.wizo.model;

public class MoodDetail {


    private int moodDetailId;

    private int moodHeaderId;

    private int localDevId;

    private String devMac;

    private int devType;

    private int operation;


    public int getMoodDetailId() {
        return moodDetailId;
    }

    public void setMoodDetailId(int moodDetailId) {
        this.moodDetailId = moodDetailId;
    }

    public int getMoodHeaderId() {
        return moodHeaderId;
    }

    public void setMoodHeaderId(int moodHeaderId) {
        this.moodHeaderId = moodHeaderId;
    }

    public int getLocalDevId() {
        return localDevId;
    }

    public void setLocalDevId(int localDevId) {
        this.localDevId = localDevId;
    }

    public String getDevMac() {
        return devMac;
    }

    public void setDevMac(String devMac) {
        this.devMac = devMac;
    }

    public int getDevType() {
        return devType;
    }

    public void setDevType(int devType) {
        this.devType = devType;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "MoodDetail{" +
                "moodDetailId=" + moodDetailId +
                ", moodHeaderId=" + moodHeaderId +
                ", localDevId=" + localDevId +
                ", devMac='" + devMac + '\'' +
                ", devType=" + devType +
                ", operation=" + operation +
                '}';
    }
}
