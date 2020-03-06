package com.ats.wizo.model;

import java.util.List;

/**
 * Created by MIRACLEINFOTAINMENT on 15/03/18.
 */

public class RespScanData {


    private List<ScanDevice> scanList = null;
    private Boolean error;
    private String message;

    public List<ScanDevice> getScanList() {
        return scanList;
    }

    public void setScanList(List<ScanDevice> scanList) {
        this.scanList = scanList;
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


}
