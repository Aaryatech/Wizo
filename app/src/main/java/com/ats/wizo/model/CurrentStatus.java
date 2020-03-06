package com.ats.wizo.model;

/**
 * Created by MIRACLEINFOTAINMENT on 22/05/17.
 */

public class CurrentStatus {

    String mac, status;
    int position;

    public CurrentStatus() {
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CurrentStatus{" +
                "mac='" + mac + '\'' +
                ", status='" + status + '\'' +
                ", position=" + position +
                '}';
    }
}
