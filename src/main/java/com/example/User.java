package com.example;

import java.time.*;
import java.util.Objects;
import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public abstract class User {
    // User Fields
    private final String userId;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String passwordHash;
    private String passwordSalt;    
    private LocalDate dateOfBirth;
    private String address;
    private String phoneNumber;
    private Gender gender;
    private String nationality;
    private EmergencyContact emergencyContact;
    private BloodType bloodType;
    private String identificationNumber;
    private boolean isActive;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    private String preferredLanguage;
    private String profileImageUrl;
    private final FeedbackManager feedbackManager;
    protected ChatClient chatClient;

    // Validation patterns
    private static final Pattern 
        EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"),
        ID_PATTERN = Pattern.compile("^[A-Za-z0-9-]{6,20}$"),
        NAME_PATTERN = Pattern.compile("^[a-zA-Z-'\\s]{2,50}$"),
        URL_PATTERN = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");

    // Enums
    public enum Role { ADMIN, DOCTOR, PATIENT }
    public enum Gender { MALE, FEMALE, OTHER }
    public enum BloodType { 
        A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE,
        AB_POSITIVE, AB_NEGATIVE, O_POSITIVE, O_NEGATIVE 
    }

    /**
     * Immutable emergency contact information
     */
    public static final class EmergencyContact {
        private final String name;
        private final String phoneNumber;
        private final String relationship;
        private final String email;

        public EmergencyContact(String name, String phoneNumber, String relationship, String email) {
            this.name = validate(name, NAME_PATTERN, "Invalid contact name");
            this.phoneNumber = phoneNumber;
            this.relationship = Objects.requireNonNull(relationship, "Relationship cannot be null");
            this.email=Objects.requireNonNull(email, "EMAIL can't be null");
        }

        public String getEmail() {return email;}
        public String getName() { return name; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getRelationship() { return relationship; }

        @Override
        public String toString() {
            return String.format(
                "EmergencyContact[name='%s', phone='%s', relationship='%s']",
                name, phoneNumber, relationship
            );
        }
    }

    /**
     * Immutable contact information container
     */
    public static final class ContactInfo {
        private final String email;
        private final String phoneNumber;
        private final String fullName;

        public ContactInfo(String email, String phoneNumber, String fullName) {
            this.email = validate(email, EMAIL_PATTERN, "Invalid email format");
           this.phoneNumber=phoneNumber;
            this.fullName = validate(fullName, NAME_PATTERN, "Invalid name format");
        }

        public String getEmail() { return email; }
        public String getPhone() { return phoneNumber; }
        public String getFullName() { return fullName; }

        @Override
        public String toString() {
            return String.format(
                "ContactInfo[name='%s', email='%s', phone='%s']",
                fullName, email, phoneNumber
            );
        }
    }

    // Constructor
    protected User(String userId, String firstName, String lastName, String email, Role role,
                 String password, LocalDate dateOfBirth, String address, String phoneNumber,
                 Gender gender, String nationality, User.EmergencyContact emergencyContact,
                 BloodType bloodType, String identificationNumber, String preferredLanguage,
                 String profileImageUrl, FeedbackManager feedbackManager) {
        
        this.userId = (userId);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setRole(role);
        setPassword(password);
        setDateOfBirth(dateOfBirth);
        setAddress(address);
        this.phoneNumber=phoneNumber;
        setGender(gender);
        setNationality(nationality);
        setEmergencyContact(emergencyContact);
        setBloodType(bloodType);
        setIdentificationNumber(identificationNumber);
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        setPreferredLanguage(preferredLanguage);
        this.profileImageUrl=profileImageUrl;
        this.feedbackManager = feedbackManager;
    }

    // ================= VALIDATION HELPERS =================
    protected static String validate(String input, Pattern pattern, String errorMessage) {
        if (input == null || !pattern.matcher(input).matches()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return input;
    }

   

    // ================= PASSWORD MANAGEMENT =================
    private static final SecureRandom secureRandom = new SecureRandom();

    private static String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }

    public void updatePassword(String oldPassword, String newPassword) {
        if (!verifyPassword(oldPassword)) {
            throw new IllegalArgumentException("Incorrect current password");
        }
        setPassword(newPassword);
    }

    public boolean verifyPassword(String password) {
        String attemptedHash = hashPassword(password, this.passwordSalt);
        boolean isValid = attemptedHash.equals(this.passwordHash);
        if (isValid) recordLogin();
        return isValid;
    }

    // ================= CORE METHODS =================
    public void recordLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public int calculateAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public ContactInfo getContactInfo() {
        return new ContactInfo(this.email, this.phoneNumber, getFullName());
    }

    // ================= GETTERS =================
    public String getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public Gender getGender() { return gender; }
    public String getNationality() { return nationality; }
    public EmergencyContact getEmergencyContact() { return emergencyContact; }
    public BloodType getBloodType() { return bloodType; }
    public String getIdentificationNumber() { return identificationNumber; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public FeedbackManager getFeedbackManager() { return feedbackManager; }

    // ================= SETTERS =================
    public void setFirstName(String firstName) {
        this.firstName = validate(firstName, NAME_PATTERN, "Invalid first name");
        updateTimestamp();
    }

    public void setLastName(String lastName) {
        this.lastName = validate(lastName, NAME_PATTERN, "Invalid last name");
        updateTimestamp();
    }

    public void setEmail(String email) {
        this.email = validate(email, EMAIL_PATTERN, "Invalid email format");
        updateTimestamp();
    }

    public void setRole(Role role) {
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        updateTimestamp();
    }

    public void setGender(Gender gender) {
        this.gender = Objects.requireNonNull(gender, "Gender cannot be null");
        updateTimestamp();
    }

    public void setBloodType(BloodType bloodType) {
        this.bloodType = Objects.requireNonNull(bloodType, "Blood type cannot be null");
        updateTimestamp();
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        this.passwordSalt = generateSalt();
        this.passwordHash = hashPassword(password, this.passwordSalt);
        updateTimestamp();
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Invalid date of birth");
        }
        this.dateOfBirth = dateOfBirth;
        updateTimestamp();
    }

    public void setAddress(String address) {
        this.address = (address != null) ? address.trim() : null;
        updateTimestamp();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        updateTimestamp();
    }

    public void setNationality(String nationality) {
        this.nationality = (nationality != null) ? nationality.trim() : null;
        updateTimestamp();
    }

    public void setEmergencyContact(EmergencyContact emergencyContact) {
        this.emergencyContact = (emergencyContact != null) ?
            new EmergencyContact(
                emergencyContact.getName(),
                emergencyContact.getPhoneNumber(),
                emergencyContact.getRelationship(), emergencyContact.getEmail()
            ) : null;
        updateTimestamp();
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = validate(identificationNumber, ID_PATTERN, "Invalid ID format");
        updateTimestamp();
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = (preferredLanguage != null) ? preferredLanguage.trim() : null;
        updateTimestamp();
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        updateTimestamp();
    }

    // ================= STATUS MANAGEMENT =================
    public void deactivate() {
        this.isActive = false;
        updateTimestamp();
    }

    public void reactivate() {
        this.isActive = true;
        updateTimestamp();
    }

    // ================= UTILITIES =================
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return String.format(
            "User[ID=%d, Name='%s', Email='%s', Role=%s, Active=%s]",
            userId, getFullName(), email, role, isActive
        );
    }


    public void connectToChat(ChatServer server) {
        this.chatClient = new ChatClient(getFullName(), server);
        new Thread(chatClient).start();
    }

    public void sendMessage(String recipientId, String message) {
        if (chatClient != null) {
            System.out.printf("[%s] Sending to %s: %s%n", userId, recipientId, message);
            chatClient.sendMessage(userId, recipientId);
        } else {
            System.out.println("Not connected to chat");
        }
    }

    public void disconnectFromChat() {
        if (chatClient != null) {
            chatClient.disconnect();
            chatClient = null;
        }
    }

    public abstract void displayInfo();
}