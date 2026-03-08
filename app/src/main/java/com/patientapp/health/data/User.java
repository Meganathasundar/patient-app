package com.patientapp.health.data;

import java.io.Serializable;

public class User implements Serializable {
    private final String id;
    private final String phone;
    private final UserRole role;
    private final String displayName;
    private final String doctorId;
    private final String fcmToken;

    public User(String id, String phone, UserRole role, String displayName,
                String doctorId, String fcmToken) {
        this.id = id;
        this.phone = phone;
        this.role = role;
        this.displayName = displayName;
        this.doctorId = doctorId;
        this.fcmToken = fcmToken;
    }

    public String getId() { return id; }
    public String getPhone() { return phone; }
    public UserRole getRole() { return role; }
    public String getDisplayName() { return displayName; }
    public String getDoctorId() { return doctorId; }
    public String getFcmToken() { return fcmToken; }

    public String getIdentifier() { return phone; }
}
