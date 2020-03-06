package com.ats.wizo.model;

/**
 * Created by maxadmin on 6/1/18.
 */

public class Room {

    public Integer roomId;
    public Integer userId;
    public String roomName;
    public String roomIcon;
    public Integer roomIsUsed;

    public Room() {
    }


    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Integer getRoomIsUsed() {
        return roomIsUsed;
    }

    public void setRoomIsUsed(Integer roomIsUsed) {
        this.roomIsUsed = roomIsUsed;
    }

    public String getRoomIcon() {
        return roomIcon;
    }

    public void setRoomIcon(String roomIcon) {
        this.roomIcon = roomIcon;
    }


    @Override
    public String toString() {
        return "Room{" +
                "roomId=" + roomId +
                ", userId=" + userId +
                ", roomName='" + roomName + '\'' +
                ", roomIcon='" + roomIcon + '\'' +
                ", roomIsUsed=" + roomIsUsed +
                '}';
    }
}
