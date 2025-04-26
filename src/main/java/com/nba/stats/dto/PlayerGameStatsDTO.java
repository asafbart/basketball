package com.nba.stats.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class PlayerGameStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @NotNull
    private Long playerId;
    
    @NotNull
    private Long gameId;
    
    @NotNull
    @Min(0)
    private Integer points;
    
    @NotNull
    @Min(0)
    private Integer rebounds;
    
    @NotNull
    @Min(0)
    private Integer assists;
    
    @NotNull
    @Min(0)
    private Integer steals;
    
    @NotNull
    @Min(0)
    private Integer blocks;
    
    @NotNull
    @Min(0)
    @Max(6)
    private Integer fouls;
    
    @NotNull
    @Min(0)
    private Integer turnovers;
    
    @NotNull
    @Min(0)
    @Max(48)
    private Double minutesPlayed;
    
    public PlayerGameStatsDTO() {
    }
    
    public Long getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
    
    public Long getGameId() {
        return gameId;
    }
    
    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
    
    public Integer getPoints() {
        return points;
    }
    
    public void setPoints(Integer points) {
        this.points = points;
    }
    
    public Integer getRebounds() {
        return rebounds;
    }
    
    public void setRebounds(Integer rebounds) {
        this.rebounds = rebounds;
    }
    
    public Integer getAssists() {
        return assists;
    }
    
    public void setAssists(Integer assists) {
        this.assists = assists;
    }
    
    public Integer getSteals() {
        return steals;
    }
    
    public void setSteals(Integer steals) {
        this.steals = steals;
    }
    
    public Integer getBlocks() {
        return blocks;
    }
    
    public void setBlocks(Integer blocks) {
        this.blocks = blocks;
    }
    
    public Integer getFouls() {
        return fouls;
    }
    
    public void setFouls(Integer fouls) {
        this.fouls = fouls;
    }
    
    public Integer getTurnovers() {
        return turnovers;
    }
    
    public void setTurnovers(Integer turnovers) {
        this.turnovers = turnovers;
    }
    
    public Double getMinutesPlayed() {
        return minutesPlayed;
    }
    
    public void setMinutesPlayed(Double minutesPlayed) {
        this.minutesPlayed = minutesPlayed;
    }
}