package com.example.brightstart;

public class BillingDetail {
    private String childName;
    private String monthYear;
    private String paymentStatus;
    private long meals;
    private long resourceFees;
    private long transportation;
    private long tuitionFees;

    // Constructor
    public BillingDetail(String childName, String monthYear, String paymentStatus,
                         long meals, long resourceFees, long transportation, long tuitionFees) {
        this.childName = childName;
        this.monthYear = monthYear;
        this.paymentStatus = paymentStatus;
        this.meals = meals;
        this.resourceFees = resourceFees;
        this.transportation = transportation;
        this.tuitionFees = tuitionFees;
    }

    // Getters
    public String getChildName() { return childName; }
    public String getMonthYear() { return monthYear; }
    public String getPaymentStatus() { return paymentStatus; }
    public long getMeals() { return meals; }
    public long getResourceFees() { return resourceFees; }
    public long getTransportation() { return transportation; }
    public long getTuitionFees() { return tuitionFees; }
}

