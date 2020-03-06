package com.ats.wizo.model;

/**
 * Created by maxadmin on 12/1/18.
 */

public class ScanDevice {

    private int devId;
    private int userId;
    private String devMac;

    public ScanDevice() {
    }

    public ScanDevice(int scanDeviceId, String scanDeviceMac) {
        this.devId = scanDeviceId;
        this.devMac = scanDeviceMac;
    }

    public int getDevId() {
        return devId;
    }

    public void setDevId(int devId) {
        this.devId = devId;
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


    @Override
    public String toString() {
        return "ScanDevice{" +
                "devId=" + devId +
                ", userId=" + userId +
                ", devMac='" + devMac + '\'' +
                '}';
    }
}
