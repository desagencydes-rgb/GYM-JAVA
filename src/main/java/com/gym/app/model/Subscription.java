package com.gym.app.model;

import java.time.LocalDate;

public class Subscription {
    // Unique ID for the subscription
    private int id;

    // Code/Foreign Key linking to the Member
    private int memberId;

    // Name of the plan (e.g. "Gold", "Monthly")
    private String planName;

    // Duration of the subscription
    private LocalDate startDate;
    private LocalDate endDate;

    // Cost of the subscription
    private double price;

    // Current status (e.g. "Active", "Expired")
    private String status;

    /**
     * Constructor for Subscription.
     * 
     * @param id        Unique ID
     * @param memberId  Member's ID
     * @param planName  Plan Name
     * @param startDate Start Date
     * @param endDate   End Date
     * @param price     Price paid
     * @param status    Status of subscription
     */
    public Subscription(int id, int memberId, String planName, LocalDate startDate, LocalDate endDate, double price,
            String status) {
        this.id = id;
        this.memberId = memberId;
        this.planName = planName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getMemberId() {
        return memberId;
    }

    public String getPlanName() {
        return planName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }
}
