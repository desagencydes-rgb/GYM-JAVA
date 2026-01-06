package com.gym.app.model;

public class ScheduleItem {
    // Unique ID
    private int id;

    // Foreign Key to Coach
    private int coachId;

    // Day of the week (e.g. "Monday")
    private String day;

    // Start Time (HH:mm)
    private String startTime;

    // End Time (HH:mm)
    private String endTime;

    // Class Title (e.g. "Yoga 101")
    private String title;

    /**
     * Constructor for Schedule Item.
     * 
     * @param id        Unique ID
     * @param coachId   Coach ID
     * @param day       Day
     * @param startTime Start Time
     * @param endTime   End Time
     * @param title     Title
     */
    public ScheduleItem(int id, int coachId, String day, String startTime, String endTime, String title) {
        this.id = id;
        this.coachId = coachId;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public int getCoachId() {
        return coachId;
    }

    public String getDay() {
        return day;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getTitle() {
        return title;
    }

    private String coachName;

    public String getCoachName() {
        return coachName;
    }

    public void setCoachName(String coachName) {
        this.coachName = coachName;
    }
}
