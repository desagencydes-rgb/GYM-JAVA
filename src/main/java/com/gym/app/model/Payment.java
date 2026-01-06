package com.gym.app.model;

import java.time.LocalDate;

public class Payment {
    // Unique ID
    private int id;

    // Foreign Key to Member
    private int memberId;

    // Payment amount
    private double amount;

    // Date of transaction
    private LocalDate paymentDate;

    // Payment method (e.g. "Cash", "Card")
    private String method;

    /**
     * Constructor for Payment entity.
     * 
     * @param id          Unique ID
     * @param memberId    Member ID
     * @param amount      Amount paid
     * @param paymentDate Date of payment
     * @param method      Payment method
     */
    public Payment(int id, int memberId, double amount, LocalDate paymentDate, String method) {
        this.id = id;
        this.memberId = memberId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.method = method;
    }

    public int getId() {
        return id;
    }

    public int getMemberId() {
        return memberId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getMethod() {
        return method;
    }
}
