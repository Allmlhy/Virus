package com.dhr.maven.virus_backend.dto;

public class CityStatsDTO {

    private String city;
    private int totalConfirmed;
    private int totalDeaths;
    private int totalRecovered;
    private int totalImported;
    private int totalAsymptomatic;
    private int currentConfirmed;

    public CityStatsDTO(String city, int totalConfirmed, int totalDeaths,
                        int totalRecovered, int totalImported,
                        int totalAsymptomatic, int currentConfirmed) {
        this.city = city;
        this.totalConfirmed = totalConfirmed;
        this.totalDeaths = totalDeaths;
        this.totalRecovered = totalRecovered;
        this.totalImported = totalImported;
        this.totalAsymptomatic = totalAsymptomatic;
        this.currentConfirmed = currentConfirmed;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getTotalConfirmed() {
        return totalConfirmed;
    }

    public void setTotalConfirmed(int totalConfirmed) {
        this.totalConfirmed = totalConfirmed;
    }

    public int getTotalRecovered() {
        return totalRecovered;
    }

    public void setTotalRecovered(int totalRecovered) {
        this.totalRecovered = totalRecovered;
    }

    public int getTotalAsymptomatic() {
        return totalAsymptomatic;
    }

    public void setTotalAsymptomatic(int totalAsymptomatic) {
        this.totalAsymptomatic = totalAsymptomatic;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public void setTotalDeaths(int totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public int getTotalImported() {
        return totalImported;
    }

    public void setTotalImported(int totalImported) {
        this.totalImported = totalImported;
    }

    public int getCurrentConfirmed() {
        return currentConfirmed;
    }

    public void setCurrentConfirmed(int currentConfirmed) {
        this.currentConfirmed = currentConfirmed;
    }
}
