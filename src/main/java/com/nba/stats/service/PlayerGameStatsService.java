package com.nba.stats.service;

import com.nba.stats.dto.PlayerGameStatsDTO;
import com.nba.stats.exception.InvalidStatsException;
import com.nba.stats.exception.ResourceNotFoundException;
import com.nba.stats.model.Game;
import com.nba.stats.model.Player;
import com.nba.stats.model.PlayerGameStats;
import com.nba.stats.repository.GameRepository;
import com.nba.stats.repository.PlayerGameStatsRepository;
import com.nba.stats.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class PlayerGameStatsService {
    private final PlayerGameStatsRepository statsRepository;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final CacheService cacheService;
    
    // For thread safety
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public PlayerGameStatsService(PlayerGameStatsRepository statsRepository, 
                                 PlayerRepository playerRepository, 
                                 GameRepository gameRepository,
                                 CacheService cacheService) {
        this.statsRepository = statsRepository;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.cacheService = cacheService;
    }
    
    public PlayerGameStats logPlayerStats(PlayerGameStatsDTO statsDTO) throws SQLException {
        lock.writeLock().lock();
        try {
            validateStats(statsDTO);
            
            Player player = playerRepository.findById(statsDTO.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + statsDTO.getPlayerId()));
            
            Game game = gameRepository.findById(statsDTO.getGameId())
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + statsDTO.getGameId()));
            
            PlayerGameStats stats = new PlayerGameStats();
            stats.setPlayer(player);
            stats.setGame(game);
            stats.setPoints(statsDTO.getPoints());
            stats.setRebounds(statsDTO.getRebounds());
            stats.setAssists(statsDTO.getAssists());
            stats.setSteals(statsDTO.getSteals());
            stats.setBlocks(statsDTO.getBlocks());
            stats.setFouls(statsDTO.getFouls());
            stats.setTurnovers(statsDTO.getTurnovers());
            stats.setMinutesPlayed(statsDTO.getMinutesPlayed());
            
            PlayerGameStats savedStats = statsRepository.save(stats);
            
            // Ensure cache invalidation happens immediately after saving stats
            cacheService.invalidateStatsCache(player.getId(), player.getTeam().getId());
            
            return savedStats;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public List<PlayerGameStats> getPlayerGameStats(Long playerId) throws SQLException {
        lock.readLock().lock();
        try {
            Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));
            
            return statsRepository.findByPlayerId(playerId);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public List<PlayerGameStats> getGameStats(Long gameId) throws SQLException {
        lock.readLock().lock();
        try {
            Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + gameId));
            
            return statsRepository.findByGameId(gameId);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private void validateStats(PlayerGameStatsDTO stats) {
        if (stats.getPoints() < 0) {
            throw new InvalidStatsException("Points cannot be negative");
        }
        
        if (stats.getRebounds() < 0) {
            throw new InvalidStatsException("Rebounds cannot be negative");
        }
        
        if (stats.getAssists() < 0) {
            throw new InvalidStatsException("Assists cannot be negative");
        }
        
        if (stats.getSteals() < 0) {
            throw new InvalidStatsException("Steals cannot be negative");
        }
        
        if (stats.getBlocks() < 0) {
            throw new InvalidStatsException("Blocks cannot be negative");
        }
        
        if (stats.getFouls() < 0 || stats.getFouls() > 6) {
            throw new InvalidStatsException("Fouls must be between 0 and 6");
        }
        
        if (stats.getTurnovers() < 0) {
            throw new InvalidStatsException("Turnovers cannot be negative");
        }
        
        if (stats.getMinutesPlayed() < 0 || stats.getMinutesPlayed() > 48.0) {
            throw new InvalidStatsException("Minutes played must be between 0 and 48.0");
        }
    }
}