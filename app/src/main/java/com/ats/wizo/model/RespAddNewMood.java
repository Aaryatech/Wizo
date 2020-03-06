package com.ats.wizo.model;

public class RespAddNewMood {


    private PostNewMood mood;

    private boolean error;
    private String message;

    public PostNewMood getPostNewMood() {
        return mood;
    }

    public void setPostNewMood(PostNewMood postNewMood) {
        this.mood = postNewMood;
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
        return "RespAddNewMood{" +
                "mood=" + mood +
                ", error=" + error +
                ", message='" + message + '\'' +
                '}';
    }
}
