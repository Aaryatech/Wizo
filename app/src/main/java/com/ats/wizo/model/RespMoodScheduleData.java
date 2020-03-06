package com.ats.wizo.model;

import java.util.List;

public class RespMoodScheduleData {

    private List<RespMoodScheduler> scheduleList = null;
    private boolean error;
    private String message;

    public List<RespMoodScheduler> getSchedulerList() {
        return scheduleList;
    }

    public void setSchedulerList(List<RespMoodScheduler> schedulerList) {
        this.scheduleList = schedulerList;
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
        return "RespMoodScheduleData{" +
                "schedulerList=" + scheduleList +
                ", error=" + error +
                ", message='" + message + '\'' +
                '}';
    }
}
