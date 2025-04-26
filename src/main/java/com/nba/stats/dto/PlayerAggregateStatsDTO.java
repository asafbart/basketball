package com.nba.stats.dto;

import java.io.Serializable;

public class PlayerAggregateStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long playerId;
    private String firstName;
    private String lastName;
    private String teamName;
    private Double averagePoints;
    private Double averageRebounds;
    private Double averageAssists;
    private Double averageSteals;
    private Double averageBlocks;
    private Double averageFouls;
    private Double averageTurnovers;
    private Double averageMinutesPlayed;
    private Integer gamesPlayed;
    
    public PlayerAggregateStatsDTO() {
    }
    
    public Long getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getTeamName() {
        return teamName;
    }
    
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    
    public Double getAveragePoints() {
        return averagePoints;
    }
    
    public void setAveragePoints(Double averagePoints) {
        this.averagePoints = averagePoints;
    }
    
    public Double getAverageRebounds() {
        return averageRebounds;
    }
    
    public void setAverageRebounds(Double averageRebounds) {
        this.averageRebounds = averageRebounds;
    }
    
    public Double getAverageAssists() {
        return averageAssists;
    }
    
    public void setAverageAssists(Double averageAssists) {
        this.averageAssists = averageAssists;
    }
    
    public Double getAverageSteals() {
        return averageSteals;
    }
    
    public void setAverageSteals(Double averageSteals) {
        this.averageSteals = averageSteals;
    }
    
    public Double getAverageBlocks() {
        return averageBlocks;
    }
    
    public void setAverageBlocks(Double averageBlocks) {
        this.averageBlocks = averageBlocks;
    }
    
    public Double getAverageFouls() {
        return averageFouls;
    }
    
    public void setAverageFouls(Double averageFouls) {
        this.averageFouls = averageFouls;
    }
    
    public Double getAverageTurnovers() {
        return averageTurnovers;
    }
    
    public void setAverageTurnovers(Double averageTurnovers) {
        this.averageTurnovers = averageTurnovers;
    }
    
    public Double getAverageMinutesPlayed() {
        return averageMinutesPlayed;
    }
    
    public void setAverageMinutesPlayed(Double averageMinutesPlayed) {
        this.averageMinutesPlayed = averageMinutesPlayed;
    }
    
    public Integer getGamesPlayed() {
        return gamesPlayed;
    }
    
    public void setGamesPlayed(Integer gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }
}