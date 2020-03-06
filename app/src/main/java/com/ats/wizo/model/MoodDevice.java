package com.ats.wizo.model;

public class MoodDevice {

    private int devId;
    private String devCaption;
    private String devIp;
    private String devMac;
    private String devSsid;
    private int devType;
    private int devRoomId;
    private int devPosition;
    private int devIsUsed;
    private boolean isSelected;
    private String roomName;
    private boolean isHeader;
    private int operation;
    private int detailId;


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

    public String getDevSsid() {
        return devSsid;
    }

    public void setDevSsid(String devSsid) {
        this.devSsid = devSsid;
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
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
        return "MoodDevice{" +
                "devId=" + devId +
                ", devCaption='" + devCaption + '\'' +
                ", devIp='" + devIp + '\'' +
                ", devMac='" + devMac + '\'' +
                ", devSsid='" + devSsid + '\'' +
                ", devType=" + devType +
                ", devRoomId=" + devRoomId +
                ", devPosition=" + devPosition +
                ", devIsUsed=" + devIsUsed +
                ", isSelected=" + isSelected +
                ", roomName='" + roomName + '\'' +
                ", isHeader=" + isHeader +
                ", operation=" + operation +
                ", detailId=" + detailId +
                '}';
    }
}
