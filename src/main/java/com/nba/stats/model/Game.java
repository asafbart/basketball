package com.nba.stats.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Game {
    private Long id;
    private LocalDate date;
    private Team homeTeam;
    private Team awayTeam;
    private Integer homeScore;
    private Integer awayScore;
    private List<PlayerGameStats> playerStats = new ArrayList<>();
    
    public Game() {
    }
    
    public Game(Long id, LocalDate date, Team homeTeam, Team awayTeam, Integer homeScore, Integer awayScore) {
        this.id = id;
        this.date = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Team getHomeTeam() {
        return homeTeam;
    }
    
    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }
    
    public Team getAwayTeam() {
        return awayTeam;
    }
    
    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }
    
    public Integer getHomeScore() {
        return homeScore;
    }
    
    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }
    
    public Integer getAwayScore() {
        return awayScore;
    }
    
    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }
    
    public List<PlayerGameStats> getPlayerStats() {
        return playerStats;
    }
    
    public void setPlayerStats(List<PlayerGameStats> playerStats) {
        this.playerStats = playerStats;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(id, game.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}