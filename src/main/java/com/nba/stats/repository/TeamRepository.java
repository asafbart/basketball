package com.nba.stats.repository;

import com.nba.stats.model.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class TeamRepository {
    private final DataSource dataSource;
    
    public TeamRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public Team save(Team team) throws SQLException {
        String sql = "INSERT INTO teams (name, city) VALUES (?, ?)";
        
        if (team.getId() != null) {
            sql = "UPDATE teams SET name = ?, city = ? WHERE id = ?";
        }
        
        try (Connection conn = dataSource.getConnection()) {
            if (team.getId() == null) {
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, team.getName());
                    stmt.setString(2, team.getCity());
                    stmt.executeUpdate();
                    
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            team.setId(generatedKeys.getLong(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, team.getName());
                    stmt.setString(2, team.getCity());
                    stmt.setLong(3, team.getId());
                    stmt.executeUpdate();
                }
            }
            return team;
        }
    }
    
    public Optional<Team> findById(Long id) throws SQLException {
        String sql = "SELECT id, name, city FROM teams WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Team team = new Team();
                    team.setId(rs.getLong("id"));
                    team.setName(rs.getString("name"));
                    team.setCity(rs.getString("city"));
                    return Optional.of(team);
                } else {
                    return Optional.empty();
                }
            }
        }
    }
    
    public List<Team> findAll() throws SQLException {
        String sql = "SELECT id, name, city FROM teams";
        List<Team> teams = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Team team = new Team();
                team.setId(rs.getLong("id"));
                team.setName(rs.getString("name"));
                team.setCity(rs.getString("city"));
                teams.add(team);
            }
        }
        
        return teams;
    }
    
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM teams WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM teams";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
}