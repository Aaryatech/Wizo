package com.ats.wizo.model;

/**
 * Created by MIRACLEINFOTAINMENT on 25/01/18.
 */

public class RespScheduler {

    private int schId;
    private int userId;
    private String devMac;
    private int devType;
    private int operation;
    private int day;
    private String time;
    private int schStatus;


    public int getSchId() {
        return schId;
    }

    public void setSchId(int schId) {
        this.schId = schId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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
        return "RespScheduler{" +
                "schId=" + schId +
                ", userId=" + userId +
                ", devMac='" + devMac + '\'' +
                ", devType=" + devType +
                ", operation=" + operation +
                ", day=" + day +
                ", time='" + time + '\'' +
                ", schStatus=" + schStatus +
                '}';
    }
}
