package com.marquesevadera.lotr.Model;

/**
 * Created by ASUS on 1/22/2018.
 */
public class LaundryRate {
    private double regular = 40;
    private double handwashed = 15;
    private double heavyfabric = 15;
    private int days = 3;

    public double getFine() {
        return fine;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }

    private double fine = 5;

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public double getHandwashed() {
        return handwashed;
    }

    public void setHandwashed(double handwashed) {
        this.handwashed = handwashed;
    }

    public double getHeavyfabric() {
        return heavyfabric;
    }

    public void setHeavyfabric(double heavyfabric) {
        this.heavyfabric = heavyfabric;
    }

    public double getRegular() {
        return regular;
    }

    public void setRegular(double regular) {
        this.regular = regular;
    }
}
