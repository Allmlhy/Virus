package com.dhr.maven.virus_backend.pojo;

import java.util.Date;

public class ChinaEpidemic {
    private String country;
    private String province;
    private String city;
    private Integer confirmed;
    private Integer newConfirmed;
    private Integer currentConfirmed;
    private Integer deaths;
    private Integer newDeaths;
    private Float deathRate;
    private Integer recovered;
    private Integer newRecovered;
    private Float recoveryRate;
    private Integer suspected;
    private Integer importedCases;
    private Integer asymptomatic;
    private Date reportDate;

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Integer getConfirmed() { return confirmed; }
    public void setConfirmed(Integer confirmed) { this.confirmed = confirmed; }

    public Integer getNewConfirmed() { return newConfirmed; }
    public void setNewConfirmed(Integer newConfirmed) { this.newConfirmed = newConfirmed; }

    public Integer getCurrentConfirmed() { return currentConfirmed; }
    public void setCurrentConfirmed(Integer currentConfirmed) { this.currentConfirmed = currentConfirmed; }

    public Integer getDeaths() { return deaths; }
    public void setDeaths(Integer deaths) { this.deaths = deaths; }

    public Integer getNewDeaths() { return newDeaths; }
    public void setNewDeaths(Integer newDeaths) { this.newDeaths = newDeaths; }

    public Float getDeathRate() { return deathRate; }
    public void setDeathRate(Float deathRate) { this.deathRate = deathRate; }

    public Integer getRecovered() { return recovered; }
    public void setRecovered(Integer recovered) { this.recovered = recovered; }

    public Integer getNewRecovered() { return newRecovered; }
    public void setNewRecovered(Integer newRecovered) { this.newRecovered = newRecovered; }

    public Float getRecoveryRate() { return recoveryRate; }
    public void setRecoveryRate(Float recoveryRate) { this.recoveryRate = recoveryRate; }

    public Integer getSuspected() { return suspected; }
    public void setSuspected(Integer suspected) { this.suspected = suspected; }

    public Integer getImportedCases() { return importedCases; }
    public void setImportedCases(Integer importedCases) { this.importedCases = importedCases; }

    public Integer getAsymptomatic() { return asymptomatic; }
    public void setAsymptomatic(Integer asymptomatic) { this.asymptomatic = asymptomatic; }

    public Date getReportDate() { return reportDate; }
    public void setReportDate(Date reportDate) { this.reportDate = reportDate; }
}
