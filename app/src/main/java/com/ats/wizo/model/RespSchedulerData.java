package com.ats.wizo.model;

import java.util.List;

/**
 * Created by MIRACLEINFOTAINMENT on 25/01/18.
 */

public class RespSchedulerData {

    private List<RespScheduler> schedulerList = null;
    private boolean error;
    private String message;


    public List<RespScheduler> getSchedulerList() {
        return schedulerList;
    }

    public void setSchedulerList(List<RespScheduler> schedulerList) {
        this.schedulerList = schedulerList;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
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
        return "RespSchedulerData{" +
                "schedulerList=" + schedulerList +
                ", error=" + error +
                ", message='" + message + '\'' +
                '}';
    }
}
