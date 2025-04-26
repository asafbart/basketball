package com.nba.stats.model;

import java.util.Objects;

public class PlayerGameStats {
    private Long id;
    private Player player;
    private Game game;
    private Integer points;
    private Integer rebounds;
    private Integer assists;
    private Integer steals;
    private Integer blocks;
    private Integer fouls;  // max value: 6
    private Integer turnovers;
    private Double minutesPlayed;  // between 0 and 48.0
    
    public PlayerGameStats() {
    }
    
    public PlayerGameStats(Long id, Player player, Game game, Integer points, Integer rebounds, 
                          Integer assists, Integer steals, Integer blocks, Integer fouls, 
                          Integer turnovers, Double minutesPlayed) {
        this.id = id;
        this.player = player;
        this.game = game;
        this.points = points;
        this.rebounds = rebounds;
        this.assists = assists;
        this.steals = steals;
        this.blocks = blocks;
        this.fouls = fouls;
        this.turnovers = turnovers;
        this.minutesPlayed = minutesPlayed;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public Game getGame() {
        return game;
    }
    
    public void setGame(Game game) {
        this.game = game;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerGameStats that = (PlayerGameStats) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}