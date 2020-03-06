package com.ats.wizo.model;

public class FanStatus {

    private String mac;

    private String relay6;

    private String relay7;

    private String relay8;


    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getRelay6() {
        return relay6;
    }

    public void setRelay6(String relay6) {
        this.relay6 = relay6;
    }

    public String getRelay7() {
        return relay7;
    }

    public void setRelay7(String relay7) {
        this.relay7 = relay7;
    }

    public String getRelay8() {
        return relay8;
    }

    public void setRelay8(String relay8) {
        this.relay8 = relay8;
    }


    @Override
    public String toString() {
        return "FanStatus{" +
                "mac='" + mac + '\'' +
                ", relay6='" + relay6 + '\'' +
                ", relay7='" + relay7 + '\'' +
                ", relay8='" + relay8 + '\'' +
                '}';
    }
}
