package com.ats.wizo.model;

public class ErrorMessage {

    private boolean error;
    private String message;

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
        return "ErrorMessage{" +
                "error=" + error +
                ", message='" + message + '\'' +
                '}';
    }
}
