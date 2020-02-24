package com.marquesevadera.lotr.Model;
;
import android.app.Activity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.Toast;


import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.marquesevadera.lotr.Enums.FabricType;
import com.marquesevadera.lotr.Enums.WashType;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by ASUS on 2/18/2018.
 */
public class Transaction implements Serializable{

    private final int TRANSACTION_ID_LENGTH = 8;

    private int processingDays;

    private double w_regular=0 , w_heavy=0 , p_regular=0  , p_heavy=0; //price and weight of regular and heavy fabrics
    
    private double totweight=0 , totprice=0; //total price and total weight
    
    private String settled_date;
    private String received_date;

    private String dispatchment_date; //dispatchment ,settled and received_date date
    
    private double fine  = 0; //fine accumulated

    private String regular_washtype , heavy_washtype; //Either Handwashed or Machinewashed

    private String name; //name of customer

    private String transaction_id; //unique identifier of the transaction

    private String receivedby , settledby; //name of Staff who received_date and settled_date the transaction

    private String adminref;

    private String receivedby_ref , settledby_ref;

    private boolean settled = false;

    public String getAdminref() {
        return adminref;
    }

    public void setAdminref(String adminref) {
        this.adminref = adminref;
    }

    public String getDispatchment_date() {
        return dispatchment_date;
    }

    public void setDispatchment_date(String dispatchment_date) {
        this.dispatchment_date = dispatchment_date;
    }

    public boolean isSettled() {
        return settled;
    }


    public void setSettled(boolean settled) {
        this.settled = settled;
    }

    public String getReceivedby_ref() {
        return receivedby_ref;
    }

    public void setReceivedby_ref(String receivedby_ref) {
        this.receivedby_ref = receivedby_ref;
    }

    public String getSettledby_ref() {
        return settledby_ref;
    }

    public void setSettledby_ref(String settledby_ref) {
        this.settledby_ref = settledby_ref;
    }


    public Transaction(){

    }

    public Transaction(int processingDays){
        this.processingDays = processingDays;
        DateFormat df = new android.text.format.DateFormat();
        Date date = new Date();
        received_date = df.format("MM/dd/yyyy",date).toString();
        dispatchment_date = getDispatchDate();
        transaction_id = generateTransactionID();
    }



    public double getW_regular() {
        return w_regular;
    }

    public void setW_regular(double w_regular) {
        this.w_regular = w_regular;
    }

    public double getW_heavy() {
        return w_heavy;
    }

    public void setW_heavy(double w_heavy) {
        this.w_heavy = w_heavy;
    }

    public double getP_regular() {
        return p_regular;
    }

    public void setP_regular(double p_regular) {
        this.p_regular = p_regular;
    }

    public double getP_heavy() {
        return p_heavy;
    }

    public void setP_heavy(double p_heavy) {
        this.p_heavy = p_heavy;
    }

    public double getTotweight() {
        return totweight;
    }

    public void setTotweight(double totweight) {
        this.totweight = totweight;
    }

    public double getTotprice() {
        return totprice;
    }

    public void setTotprice(double totprice) {
        this.totprice = totprice;
    }

    public String getSettled_date() {
        return settled_date;
    }

    public void setSettled_date(String settled_date) {
        this.settled_date = settled_date;
    }

    public String getReceived_date() {
        return received_date;
    }

    public void setReceived_date(String received_date) {this.received_date = received_date;}

    public double getFine() { return fine; }

    public void setFine(double fine) { this.fine = fine; }

    public String getRegular_washtype() { return regular_washtype; }

    public void setRegular_washtype(String washType) {
        this.regular_washtype = washType;
    }

    public String getHeavy_washtype() {return heavy_washtype;}

    public void setHeavy_washtype(String washType) {
          this.heavy_washtype = washType;
    }

    public String getTransaction_id() {return transaction_id;}

    public void setTransaction_id(String transaction_id) {this.transaction_id = transaction_id;}

    public String getReceivedby() {return receivedby; }

    public void setReceivedby(String receivedby) { this.receivedby = receivedby; }

    public String getSettledby() {return settledby;}

    public void setSettledby(String settledby) {this.settledby = settledby;}

    public String getName() {
        return name;
    }

    public void setName(String name) {this.name = name;}





    public static double calcPrice(LaundryRate laundryRate , String washType , FabricType fabricType,  double weight){

        if(laundryRate ==  null) return 0;

        double regPrice = laundryRate.getRegular();
        double handwashedRate = laundryRate.getHandwashed() / 100; //since this is rate we need to divide it by 100
        double heavyFabricRate = laundryRate.getHeavyfabric() / 100;

        if(washType.equals("Machine Washed") && fabricType == FabricType.REGULAR_FABRIC){
            return regPrice * weight ;
        }
        else if(washType.equals("Machine Washed") && fabricType == FabricType.HEAVY_FABRIC){
            return ( regPrice + (regPrice * heavyFabricRate) ) * weight;
        }else if(washType.equals("Hand Washed")&& fabricType == FabricType.REGULAR_FABRIC){
            return ( regPrice + (regPrice * handwashedRate) ) * weight;
        }else if(washType.equals("Hand Washed") && fabricType == FabricType.HEAVY_FABRIC){
            return ( regPrice + (regPrice * handwashedRate) + (regPrice*handwashedRate) ) * weight ;
        }else{
            return 0;
        }

    }



    private String getDispatchDate(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, processingDays); //replace number of dates to add here
        DateFormat df = new android.text.format.DateFormat();
        return df.format("MM/dd/yyyy",c.getTime()).toString();



    }

    private String generateTransactionID(){

        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        int counter = 0;
        while (salt.length() < TRANSACTION_ID_LENGTH) { // length of the random string.
            counter++;
            if(counter == 4){
                salt.append("-");  //add '-' to the 4th index
            }else{
                int index = (int) (rnd.nextFloat() * SALTCHARS.length());
                salt.append(SALTCHARS.charAt(index));
            }

        }


        String saltStr = salt.toString();
        return saltStr;
    }

    public void CancelTransaction(DatabaseReference dr){
        dr.removeValue();
    }

    public void SettleTransaction(DatabaseReference dr ,String staffName , String staffRef){
        SimpleDateFormat sf = new SimpleDateFormat("MM/dd/yyyy");
        settled = true;
        settledby = staffName;
        settledby_ref = staffRef;
        settled_date = sf.format(new Date());
        dr.setValue(this);
    }


    public void ApplyFine(double fine){

        try {
            Date today = new Date();
            SimpleDateFormat sf = new SimpleDateFormat("MM/dd/yyyy");
            Date dispatchment = sf.parse(getDispatchment_date());

            if(today.after(dispatchment)){
                Calendar cal1 = new GregorianCalendar();
                Calendar cal2 = new GregorianCalendar();
                cal1.setTime(today);
                cal2.setTime(dispatchment);
                int daysBetween = daysBetween(cal2.getTime() , cal1.getTime());
                this.fine = fine * daysBetween;

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private int daysBetween(Date d1, Date d2) {
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

}
