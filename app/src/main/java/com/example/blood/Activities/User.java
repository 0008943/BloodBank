package com.example.blood.Activities;

public class User {
    public String name, city, number, bloodGroup, password, userId, profileImageUrl;
    public int lifeSaved;
    public String nextDonation;
    public double latitude, longitude;

    public User() {
    }

    public User(String name, String city, String number, String bloodGroup, String password, String userId) {
        this.name = name;
        this.city = city;
        this.number = number;
        this.bloodGroup = bloodGroup;
        this.password = password;
        this.userId = userId;
        this.lifeSaved = 0;
        this.nextDonation = "Not available";
        this.profileImageUrl = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public String getName() { return name; }
    public String getCity() { return city; }
    public String getBloodGroup() { return bloodGroup; }
    public String getProfileImageUrl() { return profileImageUrl; }
}
