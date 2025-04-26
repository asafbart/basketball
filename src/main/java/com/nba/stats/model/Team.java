package com.nba.stats.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Team {
    private Long id;
    private String name;
    private String city;
    private List<Player> players = new ArrayList<>();
    
    public Team() {
    }
    
    public Team(Long id, String name, String city) {
        this.id = id;
        this.name = name;
        this.city = city;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public List<Player> getPlayers() {
        return players;
    }
    
    public void setPlayers(List<Player> players) {
        this.players = players;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(id, team.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}