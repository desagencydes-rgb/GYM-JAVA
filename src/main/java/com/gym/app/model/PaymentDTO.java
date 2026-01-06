package com.gym.app.model;

import java.time.LocalDate;

public class PaymentDTO {
    // Unique ID
    private int id;

    // Member Name (resolved from ID for UI display)
    private String memberName;

    // Amount paid
    private double amount;

    // Date of payment
    private LocalDate date;

    // Payment method
    private String method;

    /**
     * Data Transfer Object for displaying payments with resolved Member Name.
     * 
     * @param id         Payment ID
     * @param memberName Member Name
     * @param amount     Amount
     * @param date       Date
     * @param method     Method
     */
    public PaymentDTO(int id, String memberName, double amount, LocalDate date, String method) {
        this.id = id;
        this.memberName = memberName;
        this.amount = amount;
        this.date = date;
        this.method = method;
    }

    public int getId() {
        return id;
    }

    public String getMemberName() {
        return memberName;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getMethod() {
        return method;
    }
}
