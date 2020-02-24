package com.marquesevadera.lotr.Model;

import java.io.Serializable;

/**
 * Created by ASUS on 1/14/2018.
 */
public class Admin implements Serializable {


    private String branch;
    private String address;
    private String name;
    private String admincode;
    private String phone;
    private String adminref;
    private int staffcount = 0;

    public int getStaffcount() {
        return staffcount;
    }

    public void setStaffcount(int staffcount) {
        this.staffcount = staffcount;
    }

    public String getAdminref() {
        return adminref;
    }

    public void setAdminref(String adminref) {
        this.adminref = adminref;
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

    public String getAdmincode() {
        return admincode;
    }

    public void setAdmincode(String admincode) {
        this.admincode = admincode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
