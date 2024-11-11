package com.example.brightstart.data;

public class Event {
    private String id; // Firestore document ID
    private String title;
    private String description;
    private String date; // Stored as "yyyy-MM-dd" format

    // No-argument constructor required for Firestore
    public Event() {
    }

    public Event(String id, String title, String description, String date) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
    }

    // Getter and Setter methods

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
