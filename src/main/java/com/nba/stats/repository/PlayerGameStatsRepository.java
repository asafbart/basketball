package com.nba.stats.repository;

import com.nba.stats.model.Game;
import com.nba.stats.model.Player;
import com.nba.stats.model.PlayerGameStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class PlayerGameStatsRepository {
    private final DataSource dataSource;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    
    public PlayerGameStatsRepository(DataSource dataSource, PlayerRepository playerRepository, 
                                    GameRepository gameRepository) {
        this.dataSource = dataSource;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
    }
    
    public PlayerGameStats save(PlayerGameStats stats) throws SQLException {
        String sql = "INSERT INTO player_game_stats (player_id, game_id, points, rebounds, assists, " +
                     "steals, blocks, fouls, turnovers, minutes_played) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        if (stats.getId() != null) {
            sql = "UPDATE player_game_stats SET player_id = ?, game_id = ?, points = ?, rebounds = ?, " +
                  "assists = ?, steals = ?, blocks = ?, fouls = ?, turnovers = ?, minutes_played = ? WHERE id = ?";
        }
        
        try (Connection conn = dataSource.getConnection()) {
            if (stats.getId() == null) {
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setLong(1, stats.getPlayer().getId());
                    stmt.setLong(2, stats.getGame().getId());
                    stmt.setInt(3, stats.getPoints());
                    stmt.setInt(4, stats.getRebounds());
                    stmt.setInt(5, stats.getAssists());
                    stmt.setInt(6, stats.getSteals());
                    stmt.setInt(7, stats.getBlocks());
                    stmt.setInt(8, stats.getFouls());
                    stmt.setInt(9, stats.getTurnovers());
                    stmt.setDouble(10, stats.getMinutesPlayed());
                    stmt.executeUpdate();
                    
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            stats.setId(generatedKeys.getLong(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, stats.getPlayer().getId());
                    stmt.setLong(2, stats.getGame().getId());
                    stmt.setInt(3, stats.getPoints());
                    stmt.setInt(4, stats.getRebounds());
                    stmt.setInt(5, stats.getAssists());
                    stmt.setInt(6, stats.getSteals());
                    stmt.setInt(7, stats.getBlocks());
                    stmt.setInt(8, stats.getFouls());
                    stmt.setInt(9, stats.getTurnovers());
                    stmt.setDouble(10, stats.getMinutesPlayed());
                    stmt.setLong(11, stats.getId());
                    stmt.executeUpdate();
                }
            }
            return stats;
        }
    }
    
    public Optional<PlayerGameStats> findById(Long id) throws SQLException {
        String sql = "SELECT pgs.id, pgs.player_id, pgs.game_id, pgs.points, pgs.rebounds, " +
                     "pgs.assists, pgs.steals, pgs.blocks, pgs.fouls, pgs.turnovers, pgs.minutes_played " +
                     "FROM player_game_stats pgs WHERE pgs.id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long playerId = rs.getLong("player_id");
                    Long gameId = rs.getLong("game_id");
                    
                    Player player = playerRepository.findById(playerId).orElse(null);
                    Game game = gameRepository.findById(gameId).orElse(null);
                    
                    PlayerGameStats stats = new PlayerGameStats();
                    stats.setId(rs.getLong("id"));
                    stats.setPlayer(player);
                    stats.setGame(game);
                    stats.setPoints(rs.getInt("points"));
                    stats.setRebounds(rs.getInt("rebounds"));
                    stats.setAssists(rs.getInt("assists"));
                    stats.setSteals(rs.getInt("steals"));
                    stats.setBlocks(rs.getInt("blocks"));
                    stats.setFouls(rs.getInt("fouls"));
                    stats.setTurnovers(rs.getInt("turnovers"));
                    stats.setMinutesPlayed(rs.getDouble("minutes_played"));
                    
                    return Optional.of(stats);
                } else {
                    return Optional.empty();
                }
            }
        }
    }
    
    public List<PlayerGameStats> findByPlayerId(Long playerId) throws SQLException {
        String sql = "SELECT pgs.id, pgs.player_id, pgs.game_id, pgs.points, pgs.rebounds, " +
                     "pgs.assists, pgs.steals, pgs.blocks, pgs.fouls, pgs.turnovers, pgs.minutes_played " +
                     "FROM player_game_stats pgs WHERE pgs.player_id = ?";
        List<PlayerGameStats> statsList = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, playerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long gameId = rs.getLong("game_id");
                    
                    Player player = playerRepository.findById(playerId).orElse(null);
                    Game game = gameRepository.findById(gameId).orElse(null);
                    
                    PlayerGameStats stats = new PlayerGameStats();
                    stats.setId(rs.getLong("id"));
                    stats.setPlayer(player);
                    stats.setGame(game);
                    stats.setPoints(rs.getInt("points"));
                    stats.setRebounds(rs.getInt("rebounds"));
                    stats.setAssists(rs.getInt("assists"));
                    stats.setSteals(rs.getInt("steals"));
                    stats.setBlocks(rs.getInt("blocks"));
                    stats.setFouls(rs.getInt("fouls"));
                    stats.setTurnovers(rs.getInt("turnovers"));
                    stats.setMinutesPlayed(rs.getDouble("minutes_played"));
                    
                    statsList.add(stats);
                }
            }
        }
        
        return statsList;
    }
    
    public List<PlayerGameStats> findByGameId(Long gameId) throws SQLException {
        String sql = "SELECT pgs.id, pgs.player_id, pgs.game_id, pgs.points, pgs.rebounds, " +
                     "pgs.assists, pgs.steals, pgs.blocks, pgs.fouls, pgs.turnovers, pgs.minutes_played " +
                     "FROM player_game_stats pgs WHERE pgs.game_id = ?";
        List<PlayerGameStats> statsList = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, gameId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long playerId = rs.getLong("player_id");
                    
                    Player player = playerRepository.findById(playerId).orElse(null);
                    Game game = gameRepository.findById(gameId).orElse(null);
                    
                    PlayerGameStats stats = new PlayerGameStats();
                    stats.setId(rs.getLong("id"));
                    stats.setPlayer(player);
                    stats.setGame(game);
                    stats.setPoints(rs.getInt("points"));
                    stats.setRebounds(rs.getInt("rebounds"));
                    stats.setAssists(rs.getInt("assists"));
                    stats.setSteals(rs.getInt("steals"));
                    stats.setBlocks(rs.getInt("blocks"));
                    stats.setFouls(rs.getInt("fouls"));
                    stats.setTurnovers(rs.getInt("turnovers"));
                    stats.setMinutesPlayed(rs.getDouble("minutes_played"));
                    
                    statsList.add(stats);
                }
            }
        }
        
        return statsList;
    }
    
    public List<PlayerGameStats> findByTeamIdAndDateRange(Long teamId, LocalDate startDate, LocalDate endDate) 
            throws SQLException {
        String sql = "SELECT pgs.id, pgs.player_id, pgs.game_id, pgs.points, pgs.rebounds, " +
                     "pgs.assists, pgs.steals, pgs.blocks, pgs.fouls, pgs.turnovers, pgs.minutes_played " +
                     "FROM player_game_stats pgs " +
                     "JOIN games g ON pgs.game_id = g.id " +
                     "JOIN players p ON pgs.player_id = p.id " +
                     "WHERE p.team_id = ? AND g.date BETWEEN ? AND ?";
        List<PlayerGameStats> statsList = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, teamId);
            stmt.setDate(2, java.sql.Date.valueOf(startDate));
            stmt.setDate(3, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long playerId = rs.getLong("player_id");
                    Long gameId = rs.getLong("game_id");
                    
                    Player player = playerRepository.findById(playerId).orElse(null);
                    Game game = gameRepository.findById(gameId).orElse(null);
                    
                    PlayerGameStats stats = new PlayerGameStats();
                    stats.setId(rs.getLong("id"));
                    stats.setPlayer(player);
                    stats.setGame(game);
                    stats.setPoints(rs.getInt("points"));
                    stats.setRebounds(rs.getInt("rebounds"));
                    stats.setAssists(rs.getInt("assists"));
                    stats.setSteals(rs.getInt("steals"));
                    stats.setBlocks(rs.getInt("blocks"));
                    stats.setFouls(rs.getInt("fouls"));
                    stats.setTurnovers(rs.getInt("turnovers"));
                    stats.setMinutesPlayed(rs.getDouble("minutes_played"));
                    
                    statsList.add(stats);
                }
            }
        }
        
        return statsList;
    }
    
    public List<PlayerGameStats> findByPlayerIdAndDateRange(Long playerId, LocalDate startDate, LocalDate endDate) 
            throws SQLException {
        String sql = "SELECT pgs.id, pgs.player_id, pgs.game_id, pgs.points, pgs.rebounds, " +
                     "pgs.assists, pgs.steals, pgs.blocks, pgs.fouls, pgs.turnovers, pgs.minutes_played " +
                     "FROM player_game_stats pgs " +
                     "JOIN games g ON pgs.game_id = g.id " +
                     "WHERE pgs.player_id = ? AND g.date BETWEEN ? AND ?";
        List<PlayerGameStats> statsList = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, playerId);
            stmt.setDate(2, java.sql.Date.valueOf(startDate));
            stmt.setDate(3, java.sql.Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long gameId = rs.getLong("game_id");
                    
                    Player player = playerRepository.findById(playerId).orElse(null);
                    Game game = gameRepository.findById(gameId).orElse(null);
                    
                    PlayerGameStats stats = new PlayerGameStats();
                    stats.setId(rs.getLong("id"));
                    stats.setPlayer(player);
                    stats.setGame(game);
                    stats.setPoints(rs.getInt("points"));
                    stats.setRebounds(rs.getInt("rebounds"));
                    stats.setAssists(rs.getInt("assists"));
                    stats.setSteals(rs.getInt("steals"));
                    stats.setBlocks(rs.getInt("blocks"));
                    stats.setFouls(rs.getInt("fouls"));
                    stats.setTurnovers(rs.getInt("turnovers"));
                    stats.setMinutesPlayed(rs.getDouble("minutes_played"));
                    
                    statsList.add(stats);
                }
            }
        }
        
        return statsList;
    }
    
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM player_game_stats WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    public void deleteAll() throws SQLException {
        String sql = "DELETE FROM player_game_stats";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
}