package com.ats.wizo.model;

/**
 * Created by eis-01 on 28/11/16.
 */
public class IpMacList {

    private String ip,dev_id;
    private String mac, caption,type,g_id, position;

    public IpMacList() {
    }

    public IpMacList(String caption, String ip, String mac) {
        this.caption = caption;
        this.ip = ip;
        this.mac = mac;
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

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDev_id() {
        return dev_id;
    }

    public void setDev_id(String dev_id) {
        this.dev_id = dev_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getG_id() {
        return g_id;
    }

    public void setG_id(String g_id) {
        this.g_id = g_id;
    }


    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "IpMacList{" +
                "ip='" + ip + '\'' +
                ", dev_id='" + dev_id + '\'' +
                ", mac='" + mac + '\'' +
                ", caption='" + caption + '\'' +
                ", type='" + type + '\'' +
                ", g_id='" + g_id + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
}
