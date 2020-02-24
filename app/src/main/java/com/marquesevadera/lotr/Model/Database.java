package com.marquesevadera.lotr.Model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by ASUS on 2/26/2018.
 */
public class Database {

    FirebaseDatabase lotrdb;
    DatabaseReference admin_tbl;
    DatabaseReference laundryRate_tbl;
    DatabaseReference staff_tbl;
    DatabaseReference staffReq_tbl;
    DatabaseReference staffIgnore_tbl;
    DatabaseReference transaction_tbl;
    DatabaseReference report_tbl;
    FirebaseAuth auth;
    FirebaseUser user;
    String userID;

    public static String ADMIN = "Admin";
    public static String LAUNDRY_RATE = "LaundryRate";
    public static String STAFF = "Staff";


    public static String STAFF_REQUEST = "StaffRequest";
    public static String TRANSACTION = "Transaction";
    public static String STAFF_IGNORE = "StaffIgnore";
    public static String REPORT = "Report";

    public DatabaseReference getReport_tbl() {
        return report_tbl;
    }

    public Database(){
        lotrdb = FirebaseDatabase.getInstance();
        admin_tbl = lotrdb.getReference(ADMIN);
        laundryRate_tbl = lotrdb.getReference(LAUNDRY_RATE);
        staff_tbl = lotrdb.getReference(STAFF);
        staffReq_tbl = lotrdb.getReference(STAFF_REQUEST);
        staffIgnore_tbl = lotrdb.getReference(STAFF_IGNORE);
        transaction_tbl = lotrdb.getReference(TRANSACTION);
        report_tbl = lotrdb.getReference(REPORT);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = user.getUid();

    }

    public FirebaseDatabase getLotrdb() {
        return lotrdb;
    }

    public DatabaseReference getAdmin_tbl() {
        return admin_tbl;
    }

    public DatabaseReference getLaundryRate_tbl() {
        return laundryRate_tbl;
    }

    public DatabaseReference getStaff_tbl() {
        return staff_tbl;
    }

    public DatabaseReference getStaffReq_tbl() {
        return staffReq_tbl;
    }

    public DatabaseReference getStaffIgnore_tbl() {
        return staffIgnore_tbl;
    }

    public DatabaseReference getTransaction_tbl() {
        return transaction_tbl;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseUser getUser() {
        return user;
    }

    public String getUserID() {
        return userID;
    }

}
