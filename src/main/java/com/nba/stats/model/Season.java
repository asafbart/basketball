package com.nba.stats.model;

import java.time.LocalDate;

public class Season {
    private Long id;
    private Integer year;
    private LocalDate startDate;
    private LocalDate endDate;
    
    public Season() {
    }
    
    public Season(Long id, Integer year, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.year = year;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public boolean includesDate(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}