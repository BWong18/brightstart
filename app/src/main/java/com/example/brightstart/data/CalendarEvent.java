package com.example.brightstart.data;

public class CalendarEvent {
    private String title;
    private String date;
    private String description;

    // Constructor
    public CalendarEvent(String title, String date, String description) {
        this.title = title;
        this.date = date;
        this.description = description;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
}

