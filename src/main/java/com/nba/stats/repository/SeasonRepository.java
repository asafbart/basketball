package com.nba.stats.repository;

import com.nba.stats.model.Season;

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

public class SeasonRepository {
    private final DataSource dataSource;
    
    public SeasonRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public Season save(Season season) throws SQLException {
        String sql = "INSERT INTO seasons (year, start_date, end_date) VALUES (?, ?, ?)";
        
        if (season.getId() != null) {
            sql = "UPDATE seasons SET year = ?, start_date = ?, end_date = ? WHERE id = ?";
        }
        
        try (Connection conn = dataSource.getConnection()) {
            if (season.getId() == null) {
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, season.getYear());
                    stmt.setDate(2, Date.valueOf(season.getStartDate()));
                    stmt.setDate(3, Date.valueOf(season.getEndDate()));
                    stmt.executeUpdate();
                    
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            season.setId(generatedKeys.getLong(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, season.getYear());
                    stmt.setDate(2, Date.valueOf(season.getStartDate()));
                    stmt.setDate(3, Date.valueOf(season.getEndDate()));
                    stmt.setLong(4, season.getId());
                    stmt.executeUpdate();
                }
            }
            return season;
        }
    }
    
    public Optional<Season> findById(Long id) throws SQLException {
        String sql = "SELECT id, year, start_date, end_date FROM seasons WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Season season = new Season();
                    season.setId(rs.getLong("id"));
                    season.setYear(rs.getInt("year"));
                    season.setStartDate(rs.getDate("start_date").toLocalDate());
                    season.setEndDate(rs.getDate("end_date").toLocalDate());
                    
                    return Optional.of(season);
                } else {
                    return Optional.empty();
                }
            }
        }
    }
    
    public Optional<Season> findByYear(Integer year) throws SQLException {
        String sql = "SELECT id, year, start_date, end_date FROM seasons WHERE year = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Season season = new Season();
                    season.setId(rs.getLong("id"));
                    season.setYear(rs.getInt("year"));
                    season.setStartDate(rs.getDate("start_date").toLocalDate());
                    season.setEndDate(rs.getDate("end_date").toLocalDate());
                    
                    return Optional.of(season);
                } else {
                    return Optional.empty();
                }
            }
        }
    }
    
    public Optional<Season> findByDate(LocalDate date) throws SQLException {
        String sql = "SELECT id, year, start_date, end_date FROM seasons WHERE ? BETWEEN start_date AND end_date";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Season season = new Season();
                    season.setId(rs.getLong("id"));
                    season.setYear(rs.getInt("year"));
                    season.setStartDate(rs.getDate("start_date").toLocalDate());
                    season.setEndDate(rs.getDate("end_date").toLocalDate());
                    
                    return Optional.of(season);
                } else {
                    return Optional.empty();
                }
            }
        }
    }
    
    public List<Season> findAll() throws SQLException {
        String sql = "SELECT id, year, start_date, end_date FROM seasons ORDER BY year DESC";
        List<Season> seasons = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Season season = new Season();
                season.setId(rs.getLong("id"));
                season.setYear(rs.getInt("year"));
                season.setStartDate(rs.getDate("start_date").toLocalDate());
                season.setEndDate(rs.getDate("end_date").toLocalDate());
                
                seasons.add(season);
            }
        }
        
        return seasons;
    }
    
    public Optional<Season> findCurrentSeason() throws SQLException {
        LocalDate today = LocalDate.now();
        return findByDate(today);
    }
    
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM seasons WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM seasons";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
}