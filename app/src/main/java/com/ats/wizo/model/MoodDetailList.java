package com.ats.wizo.model;

public class MoodDetailList {


    private Integer moodDetailId;
    private Integer moodHeaderId;
    private Integer localDevId;
    private String devMac;
    private Integer devType;
    private Integer operation;


    public Integer getMoodDetailId() {
        return moodDetailId;
    }

    public void setMoodDetailId(Integer moodDetailId) {
        this.moodDetailId = moodDetailId;
    }

    public Integer getMoodHeaderId() {
        return moodHeaderId;
    }

    public void setMoodHeaderId(Integer moodHeaderId) {
        this.moodHeaderId = moodHeaderId;
    }

    public Integer getLocalDevId() {
        return localDevId;
    }

    public void setLocalDevId(Integer localDevId) {
        this.localDevId = localDevId;
    }

    public String getDevMac() {
        return devMac;
    }

    public void setDevMac(String devMac) {
        this.devMac = devMac;
    }

    public Integer getDevType() {
        return devType;
    }

    public void setDevType(Integer devType) {
        this.devType = devType;
    }

    public Integer getOperation() {
        return operation;
    }

    public void setOperation(Integer operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "MoodDetailList{" +
                "moodDetailId=" + moodDetailId +
                ", moodHeaderId=" + moodHeaderId +
                ", localDevId=" + localDevId +
                ", devMac='" + devMac + '\'' +
                ", devType=" + devType +
                ", operation=" + operation +
                '}';
    }
}
