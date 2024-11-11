package com.example.brightstart;

public class TimetableItem {
    private String time;
    private String event;

    public TimetableItem(String time, String event) {
        this.time = time;
        this.event = event;
    }

    public String getTime() {
        return time;
    }

    public String getEvent() {
        return event;
    }
}
