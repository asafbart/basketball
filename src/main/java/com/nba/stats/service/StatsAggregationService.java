package com.nba.stats.service;

import com.nba.stats.dto.PlayerAggregateStatsDTO;
import com.nba.stats.dto.TeamAggregateStatsDTO;
import com.nba.stats.exception.ResourceNotFoundException;
import com.nba.stats.model.Player;
import com.nba.stats.model.PlayerGameStats;
import com.nba.stats.model.Season;
import com.nba.stats.model.Team;
import com.nba.stats.repository.PlayerGameStatsRepository;
import com.nba.stats.repository.PlayerRepository;
import com.nba.stats.repository.SeasonRepository;
import com.nba.stats.repository.TeamRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class StatsAggregationService {
    private final PlayerGameStatsRepository statsRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final SeasonRepository seasonRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheService cacheService;
    // For thread safety
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public StatsAggregationService(PlayerGameStatsRepository statsRepository,
                                  PlayerRepository playerRepository,
                                  TeamRepository teamRepository,
                                  SeasonRepository seasonRepository,
                                  RedisTemplate<String, Object> redisTemplate,
                                  CacheService cacheService) {
        this.statsRepository = statsRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.seasonRepository = seasonRepository;
        this.redisTemplate = redisTemplate;
        this.cacheService = cacheService;
    }
    
    @Cacheable(value = "playerSeasonStats", key = "#playerId")
    public PlayerAggregateStatsDTO getPlayerSeasonStats(Long playerId) throws SQLException {
        lock.readLock().lock();
        try {
            Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));
            
            Season currentSeason = seasonRepository.findCurrentSeason()
                .orElseThrow(() -> new ResourceNotFoundException("Current season not found"));
            
            List<PlayerGameStats> playerStats = statsRepository.findByPlayerIdAndDateRange(
                playerId, currentSeason.getStartDate(), currentSeason.getEndDate());
            
            if (playerStats.isEmpty()) {
                return createEmptyPlayerStats(player);
            }
            
            return calculatePlayerAggregateStats(player, playerStats);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Cacheable(value = "teamSeasonStats", key = "#teamId")
    public TeamAggregateStatsDTO getTeamSeasonStats(Long teamId) throws SQLException {
        lock.readLock().lock();
        try {
            Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
            
            Season currentSeason = seasonRepository.findCurrentSeason()
                .orElseThrow(() -> new ResourceNotFoundException("Current season not found"));
            
            List<PlayerGameStats> teamStats = statsRepository.findByTeamIdAndDateRange(
                teamId, currentSeason.getStartDate(), currentSeason.getEndDate());
            
            if (teamStats.isEmpty()) {
                return createEmptyTeamStats(team);
            }
            
            return calculateTeamAggregateStats(team, teamStats);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Cacheable(value = "allTeamSeasonStats")
    public List<TeamAggregateStatsDTO> getAllTeamSeasonStats() throws SQLException {
        lock.readLock().lock();
        try {
            List<Team> teams = teamRepository.findAll();
            List<TeamAggregateStatsDTO> teamStats = new ArrayList<>();
            
            for (Team team : teams) {
                teamStats.add(getTeamSeasonStats(team.getId()));
            }
            
            return teamStats;
        } finally {
            lock.readLock().unlock();
        }
    }

    
    // Delegate cache operations to CacheService to avoid circular dependencies
    public void invalidateStatsCache(Long playerId, Long teamId) {
        cacheService.invalidateStatsCache(playerId, teamId);
    }
    
    private PlayerAggregateStatsDTO calculatePlayerAggregateStats(Player player, List<PlayerGameStats> statsListPerPlayer) {
        int totalGames = statsListPerPlayer.size();
        
        int totalPoints = 0;
        int totalRebounds = 0;
        int totalAssists = 0;
        int totalSteals = 0;
        int totalBlocks = 0;
        int totalFouls = 0;
        int totalTurnovers = 0;
        double totalMinutesPlayed = 0.0;
        
        for (PlayerGameStats stats : statsListPerPlayer) {
            totalPoints += stats.getPoints();
            totalRebounds += stats.getRebounds();
            totalAssists += stats.getAssists();
            totalSteals += stats.getSteals();
            totalBlocks += stats.getBlocks();
            totalFouls += stats.getFouls();
            totalTurnovers += stats.getTurnovers();
            totalMinutesPlayed += stats.getMinutesPlayed();
        }
        
        PlayerAggregateStatsDTO dto = new PlayerAggregateStatsDTO();
        dto.setPlayerId(player.getId());
        dto.setFirstName(player.getFirstName());
        dto.setLastName(player.getLastName());
        dto.setTeamName(player.getTeam().getName());
        dto.setGamesPlayed(totalGames);
        
        dto.setAveragePoints((double) totalPoints / totalGames);
        dto.setAverageRebounds((double) totalRebounds / totalGames);
        dto.setAverageAssists((double) totalAssists / totalGames);
        dto.setAverageSteals((double) totalSteals / totalGames);
        dto.setAverageBlocks((double) totalBlocks / totalGames);
        dto.setAverageFouls((double) totalFouls / totalGames);
        dto.setAverageTurnovers((double) totalTurnovers / totalGames);
        dto.setAverageMinutesPlayed(totalMinutesPlayed / totalGames);
        
        return dto;
    }
    
    private TeamAggregateStatsDTO calculateTeamAggregateStats(Team team, List<PlayerGameStats> statsListPerTeam) {
        Map<Long, Integer> gamesPlayedByPlayer = new HashMap<>();
        Map<Long, List<PlayerGameStats>> statsByPlayer = new HashMap<>();
        
        // Group stats by player
        for (PlayerGameStats stats : statsListPerTeam) {
            Long playerId = stats.getPlayer().getId();
            gamesPlayedByPlayer.merge(playerId, 1, Integer::sum);
            
            statsByPlayer.computeIfAbsent(playerId, k -> new ArrayList<>()).add(stats);
        }
        
        int totalPlayers = statsByPlayer.size();
        
        double totalAveragePoints = 0;
        double totalAverageRebounds = 0;
        double totalAverageAssists = 0;
        double totalAverageSteals = 0;
        double totalAverageBlocks = 0;
        double totalAverageFouls = 0;
        double totalAverageTurnovers = 0;
        double totalAverageMinutesPlayed = 0;
        
        int maxGamesPlayed = 0;
        
        // Calculate average stats per player, then average across team
        for (Map.Entry<Long, List<PlayerGameStats>> entry : statsByPlayer.entrySet()) {
            Long playerId = entry.getKey();
            List<PlayerGameStats> playerStats = entry.getValue();
            int gamesPlayed = gamesPlayedByPlayer.get(playerId);
            
            if (gamesPlayed > maxGamesPlayed) {
                maxGamesPlayed = gamesPlayed;
            }
            
            int playerTotalPoints = 0;
            int playerTotalRebounds = 0;
            int playerTotalAssists = 0;
            int playerTotalSteals = 0;
            int playerTotalBlocks = 0;
            int playerTotalFouls = 0;
            int playerTotalTurnovers = 0;
            double playerTotalMinutesPlayed = 0.0;
            
            for (PlayerGameStats stats : playerStats) {
                playerTotalPoints += stats.getPoints();
                playerTotalRebounds += stats.getRebounds();
                playerTotalAssists += stats.getAssists();
                playerTotalSteals += stats.getSteals();
                playerTotalBlocks += stats.getBlocks();
                playerTotalFouls += stats.getFouls();
                playerTotalTurnovers += stats.getTurnovers();
                playerTotalMinutesPlayed += stats.getMinutesPlayed();
            }
            
            totalAveragePoints += (double) playerTotalPoints / gamesPlayed;
            totalAverageRebounds += (double) playerTotalRebounds / gamesPlayed;
            totalAverageAssists += (double) playerTotalAssists / gamesPlayed;
            totalAverageSteals += (double) playerTotalSteals / gamesPlayed;
            totalAverageBlocks += (double) playerTotalBlocks / gamesPlayed;
            totalAverageFouls += (double) playerTotalFouls / gamesPlayed;
            totalAverageTurnovers += (double) playerTotalTurnovers / gamesPlayed;
            totalAverageMinutesPlayed += playerTotalMinutesPlayed / gamesPlayed;
        }
        
        TeamAggregateStatsDTO dto = new TeamAggregateStatsDTO();
        dto.setTeamId(team.getId());
        dto.setTeamName(team.getName());
        dto.setCity(team.getCity());
        dto.setNumberOfPlayers(totalPlayers);
        dto.setGamesPlayed(maxGamesPlayed);
        
        dto.setAveragePoints(totalAveragePoints / totalPlayers);
        dto.setAverageRebounds(totalAverageRebounds / totalPlayers);
        dto.setAverageAssists(totalAverageAssists / totalPlayers);
        dto.setAverageSteals(totalAverageSteals / totalPlayers);
        dto.setAverageBlocks(totalAverageBlocks / totalPlayers);
        dto.setAverageFouls(totalAverageFouls / totalPlayers);
        dto.setAverageTurnovers(totalAverageTurnovers / totalPlayers);
        dto.setAverageMinutesPlayed(totalAverageMinutesPlayed / totalPlayers);
        
        return dto;
    }
    
    private PlayerAggregateStatsDTO createEmptyPlayerStats(Player player) {
        PlayerAggregateStatsDTO dto = new PlayerAggregateStatsDTO();
        dto.setPlayerId(player.getId());
        dto.setFirstName(player.getFirstName());
        dto.setLastName(player.getLastName());
        dto.setTeamName(player.getTeam().getName());
        dto.setGamesPlayed(0);
        dto.setAveragePoints(0.0);
        dto.setAverageRebounds(0.0);
        dto.setAverageAssists(0.0);
        dto.setAverageSteals(0.0);
        dto.setAverageBlocks(0.0);
        dto.setAverageFouls(0.0);
        dto.setAverageTurnovers(0.0);
        dto.setAverageMinutesPlayed(0.0);
        return dto;
    }
    
    private TeamAggregateStatsDTO createEmptyTeamStats(Team team) {
        TeamAggregateStatsDTO dto = new TeamAggregateStatsDTO();
        dto.setTeamId(team.getId());
        dto.setTeamName(team.getName());
        dto.setCity(team.getCity());
        dto.setNumberOfPlayers(0);
        dto.setGamesPlayed(0);
        dto.setAveragePoints(0.0);
        dto.setAverageRebounds(0.0);
        dto.setAverageAssists(0.0);
        dto.setAverageSteals(0.0);
        dto.setAverageBlocks(0.0);
        dto.setAverageFouls(0.0);
        dto.setAverageTurnovers(0.0);
        dto.setAverageMinutesPlayed(0.0);
        return dto;
    }
}