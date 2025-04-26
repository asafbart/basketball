package com.nba.stats.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class for repository tests with methods to create and reset tables
 */
@Component
public class AbstractRepositoryExtension {

    @Autowired
    private DataSource dataSource;
    
    /**
     * Creates the necessary tables for tests
     */
    public void createTables() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Drop tables if they exist
            stmt.executeUpdate("DROP TABLE IF EXISTS player_game_stats");
            stmt.executeUpdate("DROP TABLE IF EXISTS games");
            stmt.executeUpdate("DROP TABLE IF EXISTS players");
            stmt.executeUpdate("DROP TABLE IF EXISTS teams");
            stmt.executeUpdate("DROP TABLE IF EXISTS seasons");
            
            // Create teams table
            stmt.executeUpdate(
                "CREATE TABLE teams (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "city VARCHAR(100) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)"
            );
            
            // Create players table
            stmt.executeUpdate(
                "CREATE TABLE players (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "first_name VARCHAR(100) NOT NULL, " +
                "last_name VARCHAR(100) NOT NULL, " +
                "position VARCHAR(50), " +
                "team_id BIGINT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (team_id) REFERENCES teams(id))"
            );
            
            // Create seasons table
            stmt.executeUpdate(
                "CREATE TABLE seasons (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "year INT NOT NULL, " +
                "start_date DATE NOT NULL, " +
                "end_date DATE NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "UNIQUE (year))"
            );
            
            // Create games table
            stmt.executeUpdate(
                "CREATE TABLE games (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "date DATE NOT NULL, " +
                "home_team_id BIGINT NOT NULL, " +
                "away_team_id BIGINT NOT NULL, " +
                "home_score INT NOT NULL DEFAULT 0, " +
                "away_score INT NOT NULL DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (home_team_id) REFERENCES teams(id), " +
                "FOREIGN KEY (away_team_id) REFERENCES teams(id))"
            );
            
            // Create player game stats table
            stmt.executeUpdate(
                "CREATE TABLE player_game_stats (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "player_id BIGINT NOT NULL, " +
                "game_id BIGINT NOT NULL, " +
                "points INT NOT NULL DEFAULT 0, " +
                "rebounds INT NOT NULL DEFAULT 0, " +
                "assists INT NOT NULL DEFAULT 0, " +
                "steals INT NOT NULL DEFAULT 0, " +
                "blocks INT NOT NULL DEFAULT 0, " +
                "fouls INT NOT NULL DEFAULT 0, " +
                "turnovers INT NOT NULL DEFAULT 0, " +
                "minutes_played DECIMAL(4,1) NOT NULL DEFAULT 0.0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (player_id) REFERENCES players(id), " +
                "FOREIGN KEY (game_id) REFERENCES games(id), " +
                "UNIQUE (player_id, game_id))"
            );
        }
    }
    
    /**
     * Truncates all tables (clears data but keeps structure)
     */
    public void truncateTables() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Disable foreign key checks to allow truncating
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            
            // Truncate tables
            stmt.executeUpdate("TRUNCATE TABLE player_game_stats");
            stmt.executeUpdate("TRUNCATE TABLE games");
            stmt.executeUpdate("TRUNCATE TABLE players");
            stmt.executeUpdate("TRUNCATE TABLE teams");
            stmt.executeUpdate("TRUNCATE TABLE seasons");
            
            // Re-enable foreign key checks
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
}