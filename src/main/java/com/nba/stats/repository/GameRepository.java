package com.nba.stats.repository;

import com.nba.stats.model.Game;
import com.nba.stats.model.Team;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class GameRepository {
    private final DataSource dataSource;
    private final TeamRepository teamRepository;
    
    public GameRepository(DataSource dataSource, TeamRepository teamRepository) {
        this.dataSource = dataSource;
        this.teamRepository = teamRepository;
    }
    
    public Game save(Game game) throws SQLException {
        String sql = "INSERT INTO games (date, home_team_id, away_team_id, home_score, away_score) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        if (game.getId() != null) {
            sql = "UPDATE games SET date = ?, home_team_id = ?, away_team_id = ?, " +
                  "home_score = ?, away_score = ? WHERE id = ?";
        }
        
        try (Connection conn = dataSource.getConnection()) {
            if (game.getId() == null) {
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setDate(1, Date.valueOf(game.getDate()));
                    stmt.setLong(2, game.getHomeTeam().getId());
                    stmt.setLong(3, game.getAwayTeam().getId());
                    stmt.setInt(4, game.getHomeScore());
                    stmt.setInt(5, game.getAwayScore());
                    stmt.executeUpdate();
                    
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            game.setId(generatedKeys.getLong(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setDate(1, Date.valueOf(game.getDate()));
                    stmt.setLong(2, game.getHomeTeam().getId());
                    stmt.setLong(3, game.getAwayTeam().getId());
                    stmt.setInt(4, game.getHomeScore());
                    stmt.setInt(5, game.getAwayScore());
                    stmt.setLong(6, game.getId());
                    stmt.executeUpdate();
                }
            }
            return game;
        }
    }
    
    public Optional<Game> findById(Long id) throws SQLException {
        String sql = "SELECT g.id, g.date, g.home_team_id, g.away_team_id, g.home_score, g.away_score " +
                     "FROM games g WHERE g.id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long homeTeamId = rs.getLong("home_team_id");
                    Long awayTeamId = rs.getLong("away_team_id");
                    
                    Team homeTeam = teamRepository.findById(homeTeamId).orElse(null);
                    Team awayTeam = teamRepository.findById(awayTeamId).orElse(null);
                    
                    Game game = new Game();
                    game.setId(rs.getLong("id"));
                    game.setDate(rs.getDate("date").toLocalDate());
                    game.setHomeTeam(homeTeam);
                    game.setAwayTeam(awayTeam);
                    game.setHomeScore(rs.getInt("home_score"));
                    game.setAwayScore(rs.getInt("away_score"));
                    
                    return Optional.of(game);
                } else {
                    return Optional.empty();
                }
            }
        }
    }
    
    public List<Game> findAll() throws SQLException {
        String sql = "SELECT g.id, g.date, g.home_team_id, g.away_team_id, g.home_score, g.away_score FROM games g";
        List<Game> games = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Long homeTeamId = rs.getLong("home_team_id");
                Long awayTeamId = rs.getLong("away_team_id");
                
                Team homeTeam = teamRepository.findById(homeTeamId).orElse(null);
                Team awayTeam = teamRepository.findById(awayTeamId).orElse(null);
                
                Game game = new Game();
                game.setId(rs.getLong("id"));
                game.setDate(rs.getDate("date").toLocalDate());
                game.setHomeTeam(homeTeam);
                game.setAwayTeam(awayTeam);
                game.setHomeScore(rs.getInt("home_score"));
                game.setAwayScore(rs.getInt("away_score"));
                
                games.add(game);
            }
        }
        
        return games;
    }
    
    public List<Game> findByTeamId(Long teamId) throws SQLException {
        String sql = "SELECT g.id, g.date, g.home_team_id, g.away_team_id, g.home_score, g.away_score " +
                     "FROM games g WHERE g.home_team_id = ? OR g.away_team_id = ?";
        List<Game> games = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, teamId);
            stmt.setLong(2, teamId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long homeTeamId = rs.getLong("home_team_id");
                    Long awayTeamId = rs.getLong("away_team_id");
                    
                    Team homeTeam = teamRepository.findById(homeTeamId).orElse(null);
                    Team awayTeam = teamRepository.findById(awayTeamId).orElse(null);
                    
                    Game game = new Game();
                    game.setId(rs.getLong("id"));
                    game.setDate(rs.getDate("date").toLocalDate());
                    game.setHomeTeam(homeTeam);
                    game.setAwayTeam(awayTeam);
                    game.setHomeScore(rs.getInt("home_score"));
                    game.setAwayScore(rs.getInt("away_score"));
                    
                    games.add(game);
                }
            }
        }
        
        return games;
    }
    
    public List<Game> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT g.id, g.date, g.home_team_id, g.away_team_id, g.home_score, g.away_score " +
                     "FROM games g WHERE g.date BETWEEN ? AND ?";
        List<Game> games = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long homeTeamId = rs.getLong("home_team_id");
                    Long awayTeamId = rs.getLong("away_team_id");
                    
                    Team homeTeam = teamRepository.findById(homeTeamId).orElse(null);
                    Team awayTeam = teamRepository.findById(awayTeamId).orElse(null);
                    
                    Game game = new Game();
                    game.setId(rs.getLong("id"));
                    game.setDate(rs.getDate("date").toLocalDate());
                    game.setHomeTeam(homeTeam);
                    game.setAwayTeam(awayTeam);
                    game.setHomeScore(rs.getInt("home_score"));
                    game.setAwayScore(rs.getInt("away_score"));
                    
                    games.add(game);
                }
            }
        }
        
        return games;
    }
    
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM games WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM games";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
}