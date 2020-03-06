package com.ats.wizo.model;

/**
 * Created by maxadmin on 6/1/18.
 */

public class Router {

    private int routerId;
    private String ssid;
    private String pwd;

    public Router() {
    }

    public Router(int routerId, String ssid, String pwd) {
        this.routerId = routerId;
        this.ssid = ssid;
        this.pwd = pwd;
    }

    public Router(String ssid, String pwd) {
        this.ssid = ssid;
        this.pwd = pwd;
    }

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return "Router{" +
                "routerId=" + routerId +
                ", ssid='" + ssid + '\'' +
                ", pwd='" + pwd + '\'' +
                '}';
    }
}
