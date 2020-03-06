package com.ats.wizo.model;

import java.util.List;

/**
 * Created by MIRACLEINFOTAINMENT on 25/01/18.
 */

public class RespDeviceData {

    public List<RespDevice> deviceList = null;
    public Boolean error;
    public String message;


    public List<RespDevice> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<RespDevice> deviceList) {
        this.deviceList = deviceList;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "RespDeviceData{" +
                "deviceList=" + deviceList +
                ", error=" + error +
                ", message='" + message + '\'' +
                '}';
    }
}
