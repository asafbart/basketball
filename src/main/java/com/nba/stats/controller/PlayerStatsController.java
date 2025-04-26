package com.nba.stats.controller;

import com.nba.stats.dto.PlayerAggregateStatsDTO;
import com.nba.stats.dto.PlayerGameStatsDTO;
import com.nba.stats.exception.InvalidStatsException;
import com.nba.stats.exception.ResourceNotFoundException;
import com.nba.stats.model.Player;
import com.nba.stats.model.PlayerGameStats;
import com.nba.stats.service.PlayerGameStatsService;
import com.nba.stats.service.StatsAggregationService;

import java.sql.SQLException;
import javax.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
public class PlayerStatsController {
    private final PlayerGameStatsService playerStatsService;
    private final StatsAggregationService aggregationService;
    
    public PlayerStatsController(PlayerGameStatsService playerStatsService, StatsAggregationService aggregationService) {
        this.playerStatsService = playerStatsService;
        this.aggregationService = aggregationService;
    }
    
    @PostMapping("/{playerId}/stats")
    public ResponseEntity<PlayerGameStats> logPlayerStats(
            @PathVariable Long playerId,
            @Valid @RequestBody PlayerGameStatsDTO statsDTO) {
        try {
            statsDTO.setPlayerId(playerId);
            PlayerGameStats savedStats = playerStatsService.logPlayerStats(statsDTO);
            
            // Get the player's team ID for targeted cache invalidation
            Long teamId = savedStats.getPlayer().getTeam().getId();
            
            // Targeted cache invalidation for affected player and team
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
    
    @GetMapping("/{playerId}/stats")
    public ResponseEntity<List<PlayerGameStats>> getPlayerStats(@PathVariable Long playerId) {
        try {
            List<PlayerGameStats> playerStats = playerStatsService.getPlayerGameStats(playerId);
            return new ResponseEntity<>(playerStats, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SQLException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{playerId}/season-stats")
    public ResponseEntity<PlayerAggregateStatsDTO> getPlayerSeasonStats(@PathVariable Long playerId) {
        try {
            PlayerAggregateStatsDTO stats = aggregationService.getPlayerSeasonStats(playerId);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SQLException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}