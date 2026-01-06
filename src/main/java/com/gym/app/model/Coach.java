package com.gym.app.model;

public class Coach {
    // Unique ID
    private int id;

    // Coach's full name
    private String name;

    // Coach's area of expertise (e.g. "Yoga", "Cardio")
    private String specialization;

    // Contact phone number
    private String phone;

    /**
     * Constructor for Coach object.
     * 
     * @param id             Unique ID
     * @param name           Name
     * @param specialization Specialization
     * @param phone          Phone Number
     */
    public Coach(int id, String name, String specialization, String phone) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getPhone() {
        return phone;
    }
}
