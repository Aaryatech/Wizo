package com.ats.wizo.model;

/**
 * Created by maxadmin on 6/1/18.
 */

public class Device {

    private int devId;
    private String devCaption;
    private String devIp;
    private String devMac;
    private String devSsid;
    private int devType;
    private int devRoomId;
    private int devPosition;
    private int devIsUsed;

    private int operation;
    private int detailId;




    public Device() {
    }

    public Device(int devId, String devCaption, String devIp, String devMac, String devSsid, int devType, int devRoomId, int devPosition, int devIsUsed) {
        this.devId = devId;
        this.devCaption = devCaption;
        this.devIp = devIp;
        this.devMac = devMac;
        this.devSsid = devSsid;
        this.devType = devType;
        this.devRoomId = devRoomId;
        this.devPosition = devPosition;
        this.devIsUsed = devIsUsed;
    }

    public Device(String devCaption, String devIp, String devMac, String devSsid, int devType, int devRoomId, int devPosition, int devIsUsed) {
        this.devCaption = devCaption;
        this.devIp = devIp;
        this.devMac = devMac;
        this.devSsid = devSsid;
        this.devType = devType;
        this.devRoomId = devRoomId;
        this.devPosition = devPosition;
        this.devIsUsed = devIsUsed;
    }

    public Device(String devCaption, String devIp, String devMac, String devSsid, int devType, int devRoomId, int devPosition, int devIsUsed,int operation,int detailId) {
        this.devCaption = devCaption;
        this.devIp = devIp;
        this.devMac = devMac;
        this.devSsid = devSsid;
        this.devType = devType;
        this.devRoomId = devRoomId;
        this.devPosition = devPosition;
        this.devIsUsed = devIsUsed;
        this.operation = operation;
        this.detailId = detailId;

    }


    public int getDevId() {
        return devId;
    }

    public void setDevId(int devId) {
        this.devId = devId;
    }

    public String getDevCaption() {
        return devCaption;
    }

    public void setDevCaption(String devCaption) {
        this.devCaption = devCaption;
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

    public int getDevType() {
        return devType;
    }

    public void setDevType(int devType) {
        this.devType = devType;
    }

    public int getDevRoomId() {
        return devRoomId;
    }

    public void setDevRoomId(int devRoomId) {
        this.devRoomId = devRoomId;
    }

    public int getDevPosition() {
        return devPosition;
    }

    public void setDevPosition(int devPosition) {
        this.devPosition = devPosition;
    }

    public int getDevIsUsed() {
        return devIsUsed;
    }

    public void setDevIsUsed(int devIsUsed) {
        this.devIsUsed = devIsUsed;
    }

    public String getDevSsid() {
        return devSsid;
    }

    public void setDevSsid(String devSsid) {
        this.devSsid = devSsid;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public int getDetailId() {
        return detailId;
    }

    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }

    @Override
    public String toString() {
        return "Device{" +
                "devId=" + devId +
                ", devCaption='" + devCaption + '\'' +
                ", devIp='" + devIp + '\'' +
                ", devMac='" + devMac + '\'' +
                ", devSsid='" + devSsid + '\'' +
                ", devType=" + devType +
                ", devRoomId=" + devRoomId +
                ", devPosition=" + devPosition +
                ", devIsUsed=" + devIsUsed +
                ", operation=" + operation +
                ", detailId=" + detailId +
                '}';
    }
}
