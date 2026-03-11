package com.example.blood.Activities;

public class DonationRequest {
    private String requestId;
    private String userName;
    private String bloodGroup;
    private String units;
    private String location;
    private String userMobile;
    private long timestamp;
    private String status; // pending, accepted, completed
    private String acceptedBy; // donor mobile number

    public DonationRequest() {
    }

    public DonationRequest(String requestId, String userName, String bloodGroup, String units, String location, String userMobile, long timestamp) {
        this.requestId = requestId;
        this.userName = userName;
        this.bloodGroup = bloodGroup;
        this.units = units;
        this.location = location;
        this.userMobile = userMobile;
        this.timestamp = timestamp;
        this.status = "pending";
        this.acceptedBy = "";
    }

    public String getRequestId() { return requestId; }
    public String getUserName() { return userName; }
    public String getBloodGroup() { return bloodGroup; }
    public String getUnits() { return units; }
    public String getLocation() { return location; }
    public String getUserMobile() { return userMobile; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAcceptedBy() { return acceptedBy; }
    public void setAcceptedBy(String acceptedBy) { this.acceptedBy = acceptedBy; }
}
