package com.example.brightstart.data;

public class ChildProfile {
    private String id; // Firestore document ID
    private String childName; // Renamed from 'name' to 'childName'
    private int age; // Remains as String
    private String attendanceStatus; // "Absent" or "Present"

    // No-argument constructor required for Firestore
    public ChildProfile() {
    }

    public ChildProfile(String id, String childName, int age, String attendanceStatus) {
        this.id = id;
        this.childName = childName;
        this.age = age;
        this.attendanceStatus = attendanceStatus;
    }

    // Getter and Setter methods

    public String getId() {
        return id;
    }

    public String getChildName() { // Updated getter
        return childName;
    }

    public int getAge() { // Getter remains String
        return age;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setChildName(String childName) { // Updated setter
        this.childName = childName;
    }

    public void setAge(int age) { // Setter remains String
        this.age = age;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }
}

