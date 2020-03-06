package com.ats.wizo.model;

import java.util.List;

/**
 * Created by MIRACLEINFOTAINMENT on 25/01/18.
 */

public class RespRoomData {

    public Boolean error;
    public String message;
    public List<Room> roomList = null;


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

    public List<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    @Override
    public String toString() {
        return "RespRoomData{" +
                "error=" + error +
                ", message='" + message + '\'' +
                ", roomList=" + roomList +
                '}';
    }
}
