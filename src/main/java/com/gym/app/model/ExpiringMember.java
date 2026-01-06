package com.gym.app.model;

import java.time.LocalDate;

public class ExpiringMember {
    // The member whose subscription is expiring
    private Member member;

    // The date when the subscription ends
    private LocalDate endDate;

    // Number of days remaining until expiration
    private long daysLeft;

    /**
     * Helper DTO for displaying expiring memberships in the dashboard.
     * 
     * @param member   The Member object
     * @param endDate  Expiry Date
     * @param daysLeft Days remaining
     */
    public ExpiringMember(Member member, LocalDate endDate, long daysLeft) {
        this.member = member;
        this.endDate = endDate;
        this.daysLeft = daysLeft;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public long getDaysLeft() {
        return daysLeft;
    }
}
