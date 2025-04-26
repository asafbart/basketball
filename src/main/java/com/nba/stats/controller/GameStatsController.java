package com.nba.stats.controller;

import com.nba.stats.dto.PlayerGameStatsDTO;
import com.nba.stats.exception.InvalidStatsException;
import com.nba.stats.exception.ResourceNotFoundException;
import com.nba.stats.model.PlayerGameStats;
import com.nba.stats.service.PlayerGameStatsService;
import com.nba.stats.service.StatsAggregationService;

import java.sql.SQLException;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
public class GameStatsController {
    private final PlayerGameStatsService playerStatsService;
    private final StatsAggregationService aggregationService;
    
    public GameStatsController(PlayerGameStatsService playerStatsService, StatsAggregationService aggregationService) {
        this.playerStatsService = playerStatsService;
        this.aggregationService = aggregationService;
    }
    
    @PostMapping("/{gameId}/stats")
    public ResponseEntity<PlayerGameStats> logGameStats(
            @PathVariable Long gameId,
            @Valid @RequestBody PlayerGameStatsDTO statsDTO) {
        try {
            statsDTO.setGameId(gameId);
            PlayerGameStats savedStats = playerStatsService.logPlayerStats(statsDTO);
            
            // Get the player's and team's IDs for targeted cache invalidation
            Long playerId = savedStats.getPlayer().getId();
            Long teamId = savedStats.getPlayer().getTeam().getId();
            
            // Targeted cache invalidation
            aggregationService.invalidateStatsCache(playerId, teamId);
            
            return new ResponseEntity<>(savedStats, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (InvalidStatsException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (SQLException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/{gameId}/stats/batch")
    public ResponseEntity<String> logMultipleGameStats(
            @PathVariable Long gameId,
            @Valid @RequestBody List<PlayerGameStatsDTO> statsDTOList) {
        try {
            // Track which players/teams are affected for targeted cache invalidation
            Map<Long, Long> affectedEntities = new HashMap<>();  // playerId -> teamId
            
            for (PlayerGameStatsDTO statsDTO : statsDTOList) {
                statsDTO.setGameId(gameId);
                PlayerGameStats saved = playerStatsService.logPlayerStats(statsDTO);
                affectedEntities.put(saved.getPlayer().getId(), saved.getPlayer().getTeam().getId());
            }
            
            // Targeted cache invalidation for all affected players and teams
            for (Map.Entry<Long, Long> entry : affectedEntities.entrySet()) {
                aggregationService.invalidateStatsCache(entry.getKey(), entry.getValue());
            }
            
            return new ResponseEntity<>("Successfully logged stats for " + statsDTOList.size() + " players", 
                                       HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidStatsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SQLException e) {
            return new ResponseEntity<>("Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{gameId}/stats")
    public ResponseEntity<List<PlayerGameStats>> getGameStats(@PathVariable Long gameId) {
        try {
            List<PlayerGameStats> gameStats = playerStatsService.getGameStats(gameId);
            return new ResponseEntity<>(gameStats, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SQLException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}