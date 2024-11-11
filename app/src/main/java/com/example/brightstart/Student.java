package com.example.brightstart;

public class Student {
    private String name;
    private String standard;
    private boolean isPaid;
    private String documentId;  // Store the Firestore document ID

    // Constructor
    public Student(String name, String standard, boolean isPaid, String documentId) {
        this.name = name;
        this.standard = standard;
        this.isPaid = isPaid;
        this.documentId = documentId;  // Store Firestore document ID
    }

    // Getter for documentId
    public String getDocumentId() {
        return documentId;
    }

    // Setter for documentId (if needed)
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Setter for name (if needed)
    public void setName(String name) {
        this.name = name;
    }

    // Getter for standard
    public String getStandard() {
        return standard;
    }

    // Setter for standard (if needed)
    public void setStandard(String standard) {
        this.standard = standard;
    }

    // Getter for payment status
    public boolean isPaid() {
        return isPaid;
    }

    // Setter for payment status
    public void setPaid(boolean paid) {
        this.isPaid = paid;
    }
}




