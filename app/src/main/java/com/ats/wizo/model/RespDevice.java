package com.ats.wizo.model;

/**
 * Created by MIRACLEINFOTAINMENT on 25/01/18.
 */

public class RespDevice {

    public Integer devId;
    public Integer userId;
    public String devIp;
    public String devMac;
    public String devCaption;
    public Integer devType;
    public Integer devPosition;
    public String devSsid;
    public Integer roomId;
    public Integer devIsUsed;


    public Integer getDevId() {
        return devId;
    }

    public void setDevId(Integer devId) {
        this.devId = devId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getDevIp() {
        return devIp;
    }

    public void setDevIp(String devIp) {
        this.devIp = devIp;
    }

    public String getDevMac() {
        return devMac;
    }

    public void setDevMac(String devMac) {
        this.devMac = devMac;
    }

    public String getDevCaption() {
        return devCaption;
    }

    public void setDevCaption(String devCaption) {
        this.devCaption = devCaption;
    }

    public Integer getDevType() {
        return devType;
    }

    public void setDevType(Integer devType) {
        this.devType = devType;
    }

    public Integer getDevPosition() {
        return devPosition;
    }

    public void setDevPosition(Integer devPosition) {
        this.devPosition = devPosition;
    }

    public String getDevSsid() {
        return devSsid;
    }

    public void setDevSsid(String devSsid) {
        this.devSsid = devSsid;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Integer getDevIsUsed() {
        return devIsUsed;
    }

    public void setDevIsUsed(Integer devIsUsed) {
        this.devIsUsed = devIsUsed;
    }


    @Override
    public String toString() {
        return "RespDevice{" +
                "devId=" + devId +
                ", userId=" + userId +
                ", devIp='" + devIp + '\'' +
                ", devMac='" + devMac + '\'' +
                ", devCaption='" + devCaption + '\'' +
                ", devType=" + devType +
                ", devPosition=" + devPosition +
                ", devSsid='" + devSsid + '\'' +
                ", roomId=" + roomId +
                ", devIsUsed=" + devIsUsed +
                '}';
    }
}
