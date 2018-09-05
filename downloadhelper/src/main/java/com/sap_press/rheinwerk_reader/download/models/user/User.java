package com.sap_press.rheinwerk_reader.download.models.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sap_press.rheinwerk_reader.download.util.Converters;

import java.io.Serializable;
import java.util.List;


public class User implements Serializable{

    @SerializedName("last_name")
    @Expose
    private String lastName;
    @SerializedName("_links")
    @Expose
    private List<Link> links = null;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("is_active")
    @Expose
    private boolean isActive;
    @SerializedName("customer_id")
    @Expose
    private long customerId;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getLinkString() {
        return Converters.fromArrayList(links);
    }

    public void setLinks(String links) {
        this.links = Converters.fromString(links, Link[].class);
    }

}