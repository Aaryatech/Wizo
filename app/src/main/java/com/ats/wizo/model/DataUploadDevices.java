package com.ats.wizo.model;

/**
 * Created by MIRACLEINFOTAINMENT on 25/01/18.
 */

public class DataUploadDevices {

    private int deviceId;
    private int userId;
    private String devIp;
    private String devMac;
    private String devCaption;
    private int devType;
    private int devPosition;
    private String devSsid;
    private int roomId;
    private int devIsUsed;


    public DataUploadDevices( int userId, String devIp, String devMac, String devCaption, int devType, int devPosition, String devSsid, int roomId, int devIsUsed) {
        this.userId = userId;
        this.devIp = devIp;
        this.devMac = devMac;
        this.devCaption = devCaption;
        this.devType = devType;
        this.devPosition = devPosition;
        this.devSsid = devSsid;
        this.roomId = roomId;
        this.devIsUsed = devIsUsed;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
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

    public int getDevType() {
        return devType;
    }

    public void setDevType(int devType) {
        this.devType = devType;
    }

    public int getDevPosition() {
        return devPosition;
    }

    public void setDevPosition(int devPosition) {
        this.devPosition = devPosition;
    }

    public String getDevSsid() {
        return devSsid;
    }

    public void setDevSsid(String devSsid) {
        this.devSsid = devSsid;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getDevIsUsed() {
        return devIsUsed;
    }

    public void setDevIsUsed(int devIsUsed) {
        this.devIsUsed = devIsUsed;
    }


    @Override
    public String toString() {
        return "DataUploadDevices{" +
                "deviceId=" + deviceId +
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

