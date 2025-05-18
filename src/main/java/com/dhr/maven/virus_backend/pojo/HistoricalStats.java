package com.dhr.maven.virus_backend.pojo;

import javax.persistence.*;

// HistoricalStats.java
@Entity
@Table(name = "historical_stats")
public class HistoricalStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "region_id", referencedColumnName = "region_id")
    private Region region;

    @ManyToOne
    @JoinColumn(name = "date_id", referencedColumnName = "date_id")
    private DateDim date;

    @Column(name = "total_confirmed")
    private Integer totalConfirmed = 0;

    @Column(name = "total_deaths")
    private Integer totalDeaths = 0;

    @Column(name = "total_recovered")
    private Integer totalRecovered = 0;

    @Column(name = "total_imported")
    private Integer totalImported = 0;

    @Column(name = "total_asymptomatic")
    private Integer totalAsymptomatic = 0;

    @Column(name = "current_confirmed")
    private Integer currentConfirmed = 0;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Integer getTotalConfirmed() {
        return totalConfirmed;
    }

    public Integer getTotalDeaths() {
        return totalDeaths;
    }

    public Integer getTotalRecovered() {
        return totalRecovered;
    }

    public Integer getTotalImported() {
        return totalImported;
    }

    public Integer getTotalAsymptomatic() {
        return totalAsymptomatic;
    }

    public Integer getCurrentConfirmed() {
        return currentConfirmed;
    }

}