package com.ats.wizo.model;

/**
 * Created by eis-01 on 28/2/17.
 */

public class Schedular {


    String schId,userId,deviceId,timeStamp,status,caption;
    Integer reqOp, day;

    public Schedular() {
    }

    public Schedular(String schId, String userId, String deviceId, String timeStamp, String status, String caption, Integer reqOp, Integer day) {
        this.schId = schId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.timeStamp = timeStamp;
        this.status = status;
        this.caption = caption;
        this.reqOp = reqOp;
        this.day = day;
    }


    public String getSchId() {
        return schId;
    }

    public void setSchId(String schId) {
        this.schId = schId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Integer getReqOp() {
        return reqOp;
    }

    public void setReqOp(Integer reqOp) {
        this.reqOp = reqOp;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return "Schedular{" +
                "schId='" + schId + '\'' +
                ", userId='" + userId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", status='" + status + '\'' +
                ", caption='" + caption + '\'' +
                ", reqOp=" + reqOp +
                ", day=" + day +
                '}';
    }
}
