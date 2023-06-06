package com.example.wagwan.models;

public class ChatUsers {

    String image;
    String name;
    String userId;
    String status;
    String onlineStatus;

    public ChatUsers() {
    }

    public ChatUsers(String image, String name, String userId, String status, String onlineStatus) {
        this.image = image;
        this.name = name;
        this.userId = userId;
        this.status = status;
        this.onlineStatus = onlineStatus;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }
}
