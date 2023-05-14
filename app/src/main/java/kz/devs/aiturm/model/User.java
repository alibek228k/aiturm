package kz.devs.aiturm.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class User implements Serializable {
    private String username;
    private String email;
    private String name;
    private String specialization;
    private String phoneNumber;
    private String bio;
    private String group;
    private String image;
    private String userID;
    private SignInMethod signInMethod;
    private Gender gender;
    private float sharedAmount;
    private int requestCount;

    private List<String> sendRequests;

    //Map because key is a user id value is apartmentId
    private Map<String, String> receivedRequests;
    private String apartmentID;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public float getSharedAmount() {
        return sharedAmount;
    }

    public void setSharedAmount(float sharedAmount) {
        this.sharedAmount = sharedAmount;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public SignInMethod getSignInMethod() {
        return signInMethod;
    }

    public void setSignInMethod(SignInMethod signInMethod) {
        this.signInMethod = signInMethod;
    }

    public String getApartmentID() {
        return apartmentID;
    }

    public void setApartmentID(String apartmentID) {
        this.apartmentID = apartmentID;
    }

    public List<String> getSendRequests() {
        return sendRequests;
    }

    public void setSendRequests(List<String> sendRequests) {
        this.sendRequests = sendRequests;
    }

    public Map<String, String> getReceivedRequests() {
        return receivedRequests;
    }

    public void setReceivedRequests(Map<String, String> receivedRequests) {
        this.receivedRequests = receivedRequests;
    }

    public enum Gender {
        MALE, FEMALE
    }
}
