package com.nba.stats.controller;

import com.nba.stats.dto.TeamAggregateStatsDTO;
import com.nba.stats.exception.ResourceNotFoundException;
import com.nba.stats.service.StatsAggregationService;

import java.sql.SQLException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
public class TeamStatsController {
    private final StatsAggregationService aggregationService;
    
    public TeamStatsController(StatsAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }
    
    @GetMapping("/{teamId}/season-stats")
    public ResponseEntity<TeamAggregateStatsDTO> getTeamSeasonStats(@PathVariable Long teamId) {
        try {
            TeamAggregateStatsDTO stats = aggregationService.getTeamSeasonStats(teamId);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SQLException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/season-stats")
    public ResponseEntity<List<TeamAggregateStatsDTO>> getAllTeamSeasonStats() {
        try {
            List<TeamAggregateStatsDTO> stats = aggregationService.getAllTeamSeasonStats();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}