package com.dhr.maven.virus_backend.pojo;

import javax.persistence.*;

// DailyStats.java
@Entity
@Table(name = "daily_stats")
public class DailyStats {
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

    @Column(name = "new_deaths")
    private Integer newDeaths = 0;

    @Column(name = "new_confirmed")
    private Integer newConfirmed = 0;

    @Column(name = "new_recovered")
    private Integer newRecovered = 0;

    @Column(name = "new_suspected")
    private Integer newSuspected = 0;

    private Float v = 0.0f;
    private Float a = 0.0f;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
