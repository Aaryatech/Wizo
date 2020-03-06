package com.ats.wizo.model;

/**
 * Created by eis-01 on 22/2/17.
 */

public class RefreshList {

    private String ip;
    private String mac, status;


    public RefreshList() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return "RefreshList{" +
                "ip='" + ip + '\'' +
                ", mac='" + mac + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
