package com.dhr.maven.virus_backend.dto;

public class DailyStatDTO {
    private String date;
    private int newConfirmed;
    private int newDeaths;
    private int newRecovered;
    private int newSuspected;
    private float v;
    private float a;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNewConfirmed() {
        return newConfirmed;
    }

    public void setNewConfirmed(int newConfirmed) {
        this.newConfirmed = newConfirmed;
    }

    public int getNewDeaths() {
        return newDeaths;
    }

    public void setNewDeaths(int newDeaths) {
        this.newDeaths = newDeaths;
    }

    public int getNewRecovered() {
        return newRecovered;
    }

    public void setNewRecovered(int newRecovered) {
        this.newRecovered = newRecovered;
    }

    public int getNewSuspected() {
        return newSuspected;
    }

    public void setNewSuspected(int newSuspected) {
        this.newSuspected = newSuspected;
    }

    public float getV() {
        return v;
    }

    public void setV(float v) {
        this.v = v;
    }

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }
}
