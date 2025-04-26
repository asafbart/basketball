package com.nba.stats.dto;

import java.io.Serializable;

public class TeamAggregateStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long teamId;
    private String teamName;
    private String city;
    private Double averagePoints;
    private Double averageRebounds;
    private Double averageAssists;
    private Double averageSteals;
    private Double averageBlocks;
    private Double averageFouls;
    private Double averageTurnovers;
    private Double averageMinutesPlayed;
    private Integer numberOfPlayers;
    private Integer gamesPlayed;
    
    public TeamAggregateStatsDTO() {
    }
    
    public Long getTeamId() {
        return teamId;
    }
    
    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
    
    public String getTeamName() {
        return teamName;
    }
    
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
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
    
    public Integer getNumberOfPlayers() {
        return numberOfPlayers;
    }
    
    public void setNumberOfPlayers(Integer numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }
    
    public Integer getGamesPlayed() {
        return gamesPlayed;
    }
    
    public void setGamesPlayed(Integer gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }
}