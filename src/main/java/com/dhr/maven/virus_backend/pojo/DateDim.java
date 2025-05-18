package com.dhr.maven.virus_backend.pojo;

import javax.persistence.*;

@Entity
@Table(name = "date_dim")
public class DateDim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "date_id")  // ✅ 明确绑定字段名
    private Integer dateId;

    private int year;
    private int month;
    private int day;

    // Getters and Setters
    public Integer getDateId() { return dateId; }
    public void setDateId(Integer dateId) { this.dateId = dateId; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }
}
