package com.example.blood.Activities;

public class Notification {
    private String name;
    private String message;
    private String time;
    private String avatarUrl;

    public Notification() {}

    public Notification(String name, String message, String time, String avatarUrl) {
        this.name = name;
        this.message = message;
        this.time = time;
        this.avatarUrl = avatarUrl;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
