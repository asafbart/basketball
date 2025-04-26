package com.nba.stats.repository;

import com.nba.stats.model.Player;
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

public class PlayerRepository {
    private final DataSource dataSource;
    private final TeamRepository teamRepository;
    
    public PlayerRepository(DataSource dataSource, TeamRepository teamRepository) {
        this.dataSource = dataSource;
        this.teamRepository = teamRepository;
    }
    
    public Player save(Player player) throws SQLException {
        String sql = "INSERT INTO players (first_name, last_name, position, team_id) VALUES (?, ?, ?, ?)";
        
        if (player.getId() != null) {
            sql = "UPDATE players SET first_name = ?, last_name = ?, position = ?, team_id = ? WHERE id = ?";
        }
        
        try (Connection conn = dataSource.getConnection()) {
            if (player.getId() == null) {
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, player.getFirstName());
                    stmt.setString(2, player.getLastName());
                    stmt.setString(3, player.getPosition());
                    stmt.setLong(4, player.getTeam().getId());
                    stmt.executeUpdate();
                    
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            player.setId(generatedKeys.getLong(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, player.getFirstName());
                    stmt.setString(2, player.getLastName());
                    stmt.setString(3, player.getPosition());
                    stmt.setLong(4, player.getTeam().getId());
                    stmt.setLong(5, player.getId());
                    stmt.executeUpdate();
                }
            }
            return player;
        }
    }
    
    public Optional<Player> findById(Long id) throws SQLException {
        String sql = "SELECT p.id, p.first_name, p.last_name, p.position, p.team_id " +
                     "FROM players p WHERE p.id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long teamId = rs.getLong("team_id");
                    Team team = teamRepository.findById(teamId).orElse(null);
                    
                    Player player = new Player();
                    player.setId(rs.getLong("id"));
                    player.setFirstName(rs.getString("first_name"));
                    player.setLastName(rs.getString("last_name"));
                    player.setPosition(rs.getString("position"));
                    player.setTeam(team);
                    
                    return Optional.of(player);
                } else {
                    return Optional.empty();
                }
            }
        }
    }
    
    public List<Player> findAll() throws SQLException {
        String sql = "SELECT p.id, p.first_name, p.last_name, p.position, p.team_id FROM players p";
        List<Player> players = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Long teamId = rs.getLong("team_id");
                Team team = teamRepository.findById(teamId).orElse(null);
                
                Player player = new Player();
                player.setId(rs.getLong("id"));
                player.setFirstName(rs.getString("first_name"));
                player.setLastName(rs.getString("last_name"));
                player.setPosition(rs.getString("position"));
                player.setTeam(team);
                
                players.add(player);
            }
        }
        
        return players;
    }
    
    public List<Player> findByTeamId(Long teamId) throws SQLException {
        String sql = "SELECT p.id, p.first_name, p.last_name, p.position, p.team_id " +
                     "FROM players p WHERE p.team_id = ?";
        List<Player> players = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, teamId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Team team = teamRepository.findById(teamId).orElse(null);
                    
                    Player player = new Player();
                    player.setId(rs.getLong("id"));
                    player.setFirstName(rs.getString("first_name"));
                    player.setLastName(rs.getString("last_name"));
                    player.setPosition(rs.getString("position"));
                    player.setTeam(team);
                    
                    players.add(player);
                }
            }
        }
        
        return players;
    }
    
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM players WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM players";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
}