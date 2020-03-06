package com.ats.wizo.model;

/**
 * Created by MIRACLEINFOTAINMENT on 27/01/18.
 */

public class User {

    private int userId;
    private String authKey;
    private String userName;
    private String userMobile;
    private String userEmail;
    private String userPic;
    private String userLocation;
    private int userIsUsed;


    public User() {

    }

    public User( String userName, String userMobile, String userEmail, String userLocation, int userIsUsed) {
        this.userName = userName;
        this.userMobile = userMobile;
        this.userEmail = userEmail;
        this.userLocation = userLocation;
        this.userIsUsed = userIsUsed;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public int getUserIsUsed() {
        return userIsUsed;
    }

    public void setUserIsUsed(int userIsUsed) {
        this.userIsUsed = userIsUsed;
    }

    public String getUserPic() {
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", authKey='" + authKey + '\'' +
                ", userName='" + userName + '\'' +
                ", userMobile='" + userMobile + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userPic='" + userPic + '\'' +
                ", userLocation='" + userLocation + '\'' +
                ", userIsUsed=" + userIsUsed +
                '}';
    }
}
