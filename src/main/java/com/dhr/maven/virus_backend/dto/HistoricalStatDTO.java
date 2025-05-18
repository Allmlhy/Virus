package com.dhr.maven.virus_backend.dto;

public class HistoricalStatDTO {
    private String date;
    private int totalConfirmed;
    private int totalDeaths;
    private int totalRecovered;
    private int totalImported;
    private int totalAsymptomatic;
    private int currentConfirmed;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTotalConfirmed() {
        return totalConfirmed;
    }

    public void setTotalConfirmed(int totalConfirmed) {
        this.totalConfirmed = totalConfirmed;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public void setTotalDeaths(int totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public int getTotalRecovered() {
        return totalRecovered;
    }

    public void setTotalRecovered(int totalRecovered) {
        this.totalRecovered = totalRecovered;
    }

    public int getTotalImported() {
        return totalImported;
    }

    public void setTotalImported(int totalImported) {
        this.totalImported = totalImported;
    }

    public int getTotalAsymptomatic() {
        return totalAsymptomatic;
    }

    public void setTotalAsymptomatic(int totalAsymptomatic) {
        this.totalAsymptomatic = totalAsymptomatic;
    }

    public int getCurrentConfirmed() {
        return currentConfirmed;
    }

    public void setCurrentConfirmed(int currentConfirmed) {
        this.currentConfirmed = currentConfirmed;
    }
}
