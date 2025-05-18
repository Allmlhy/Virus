package com.dhr.maven.virus_backend.dto;

public class NationalStatsDTO {
    private Integer totalConfirmed;
    private Integer totalDeaths;
    private Integer totalRecovered;
    private Integer totalImported;

    private Integer newConfirmed;
    private Integer newDeaths;
    private Integer newRecovered;
    private Integer newSuspected;

    // Getters and Setters

    public Integer getTotalConfirmed() {
        return totalConfirmed;
    }

    public void setTotalConfirmed(Integer totalConfirmed) {
        this.totalConfirmed = totalConfirmed;
    }

    public Integer getTotalDeaths() {
        return totalDeaths;
    }

    public void setTotalDeaths(Integer totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public Integer getTotalRecovered() {
        return totalRecovered;
    }

    public void setTotalRecovered(Integer totalRecovered) {
        this.totalRecovered = totalRecovered;
    }

    public Integer getNewConfirmed() {
        return newConfirmed;
    }

    public void setNewConfirmed(Integer newConfirmed) {
        this.newConfirmed = newConfirmed;
    }

    public Integer getNewSuspected() {
        return newSuspected;
    }

    public void setNewSuspected(Integer newSuspected) {
        this.newSuspected = newSuspected;
    }

    public Integer getNewRecovered() {
        return newRecovered;
    }

    public void setNewRecovered(Integer newRecovered) {
        this.newRecovered = newRecovered;
    }

    public Integer getNewDeaths() {
        return newDeaths;
    }

    public void setNewDeaths(Integer newDeaths) {
        this.newDeaths = newDeaths;
    }

    public Integer getTotalImported() {
        return totalImported;
    }

    public void setTotalImported(Integer totalImported) {
        this.totalImported = totalImported;
    }
}
