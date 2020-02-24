package com.marquesevadera.lotr.Model;

import java.io.Serializable;

/**
 * Created by ASUS on 2/1/2018.
 */
public class Staff implements Serializable{

    private String name;
    private String email;
    private String staffref;
    private String adminref;
    private String phone;
    private String branch;
    private String address;
    private int received = 0 , settled = 0;



    public int getReceived() {
        return received;
    }


    public int getSettled() {
        return settled;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStaffref() {
        return staffref;
    }

    public void setStaffref(String staffref) {
        this.staffref = staffref;
    }

    public String getAdminref() {
        return adminref;
    }

    public void setAdminref(String adminref) {
        this.adminref = adminref;
    }

    public void increaseSettled(){
        this.settled = this.settled + 1;
    }

    public void increaseReceived(){
        this.received = this.received + 1;
    }

    public void decreaseReceived() {this.received = this.received - 1;}

}
