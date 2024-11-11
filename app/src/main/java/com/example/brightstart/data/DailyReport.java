package com.example.brightstart.data;

public class DailyReport {
    private String childName;
    private String date;
    private String homework;
    private String imageUrl0;
    private String imageUrl1;
    private String meals;
    private String mood;
    private String performance;
    private String tuition;

    // Constructor
    public DailyReport(String childName, String date, String homework, String imageUrl0, String imageUrl1,
                       String meals, String mood, String performance, String tuition) {
        this.childName = childName;
        this.date = date;
        this.homework = homework;
        this.imageUrl0 = imageUrl0;
        this.imageUrl1 = imageUrl1;
        this.meals = meals;
        this.mood = mood;
        this.performance = performance;
        this.tuition = tuition;
    }

    // Getters
    public String getChildName() { return childName; }
    public String getDate() { return date; }
    public String getHomework() { return homework; }
    public String getImageUrl0() { return imageUrl0; }
    public String getImageUrl1() { return imageUrl1; }
    public String getMeals() { return meals; }
    public String getMood() { return mood; }
    public String getPerformance() { return performance; }
    public String getTuition() { return tuition; }
}

