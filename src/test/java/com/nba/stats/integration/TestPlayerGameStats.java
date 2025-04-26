package com.nba.stats.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

/**
 * Simple DTO for testing REST API responses.
 * This allows more flexibility in handling responses when the full model
 * might be difficult to deserialize.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestPlayerGameStats implements Serializable {
    private Long id;
    private Integer points;
    private Integer rebounds;
    private Integer assists;
    private Integer steals;
    private Integer blocks;
    private Integer fouls;
    private Integer turnovers;
    private Double minutesPlayed;
    
    // Empty constructor required for Jackson
    public TestPlayerGameStats() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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