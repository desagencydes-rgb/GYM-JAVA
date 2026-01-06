package com.gym.app.model;

import java.time.LocalDate;

public class Member {
    // Unique identifier for the member (Database Primary Key)
    private int id;

    // Member's personal details
    private String firstName;
    private String lastName;
    private String phone;
    private String email;

    // Unique token for facial recognition (stored in external API)
    private String faceId;

    // Demographic details
    private String gender;

    // Path to locally stored photo (legacy/backup)
    private String photoPath;

    // Date whenever the member first joined
    private LocalDate registrationDate;

    /**
     * Constructor for creating a Member object without a faceId (legacy
     * constructor).
     */
    public Member(int id, String firstName, String lastName, String phone, String email, String gender,
            String photoPath, LocalDate registrationDate) {
        this(id, firstName, lastName, phone, email, gender, photoPath, registrationDate, null);
    }

    /**
     * Full Constructor for Member object.
     * 
     * @param id               Unique ID
     * @param firstName        First Name
     * @param lastName         Last Name
     * @param phone            Contact Phone
     * @param email            Contact Email
     * @param gender           Gender
     * @param photoPath        Path to photo file
     * @param registrationDate Date of joining
     * @param faceId           Facial Recognition Token
     */
    public Member(int id, String firstName, String lastName, String phone, String email, String gender,
            String photoPath, LocalDate registrationDate, String faceId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.photoPath = photoPath;
        this.registrationDate = registrationDate;
        this.faceId = faceId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
