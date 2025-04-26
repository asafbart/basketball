package com.nba.stats.controller;

import com.nba.stats.dto.PlayerAggregateStatsDTO;
import com.nba.stats.dto.PlayerGameStatsDTO;
import com.nba.stats.exception.InvalidStatsException;
import com.nba.stats.exception.ResourceNotFoundException;
import com.nba.stats.model.Player;
import com.nba.stats.model.PlayerGameStats;
import com.nba.stats.model.Team;
import com.nba.stats.service.PlayerGameStatsService;
import com.nba.stats.service.StatsAggregationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerStatsControllerTest {

    @Mock
    private PlayerGameStatsService playerStatsService;

    @Mock
    private StatsAggregationService aggregationService;

    @InjectMocks
    private PlayerStatsController playerStatsController;

    private PlayerGameStatsDTO statsDTO;
    private PlayerGameStats savedStats;
    private Team team;
    private Player player;

    @BeforeEach
    void setUp() {
        // Setup test data
        team = new Team(1L, "Lakers", "Los Angeles");
        player = new Player(1L, "LeBron", "James", "F", team);
        
        statsDTO = new PlayerGameStatsDTO();
        statsDTO.setPlayerId(1L);
        statsDTO.setGameId(1L);
        statsDTO.setPoints(30);
        statsDTO.setRebounds(10);
        statsDTO.setAssists(8);
        statsDTO.setSteals(2);
        statsDTO.setBlocks(1);
        statsDTO.setFouls(2);
        statsDTO.setTurnovers(3);
        statsDTO.setMinutesPlayed(35.5);
        
        savedStats = new PlayerGameStats();
        savedStats.setId(1L);
        savedStats.setPlayer(player);
        savedStats.setPoints(30);
        savedStats.setRebounds(10);
        savedStats.setAssists(8);
        savedStats.setSteals(2);
        savedStats.setBlocks(1);
        savedStats.setFouls(2);
        savedStats.setTurnovers(3);
        savedStats.setMinutesPlayed(35.5);
    }

    @Test
    void logPlayerStats_shouldReturnCreatedStatus_whenSuccessful() throws SQLException {
        // Arrange
        when(playerStatsService.logPlayerStats(any(PlayerGameStatsDTO.class))).thenReturn(savedStats);
        doNothing().when(aggregationService).invalidateStatsCache(anyLong(), anyLong());

        // Act
        ResponseEntity<PlayerGameStats> response = playerStatsController.logPlayerStats(1L, statsDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedStats, response.getBody());
        verify(aggregationService).invalidateStatsCache(1L, 1L);
    }

    @Test
    void logPlayerStats_shouldReturnNotFoundStatus_whenPlayerNotFound() throws SQLException {
        // Arrange
        when(playerStatsService.logPlayerStats(any(PlayerGameStatsDTO.class)))
            .thenThrow(new ResourceNotFoundException("Player not found"));

        // Act
        ResponseEntity<PlayerGameStats> response = playerStatsController.logPlayerStats(1L, statsDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void logPlayerStats_shouldReturnBadRequestStatus_whenInvalidStats() throws SQLException {
        // Arrange
        when(playerStatsService.logPlayerStats(any(PlayerGameStatsDTO.class)))
            .thenThrow(new InvalidStatsException("Invalid stats"));

        // Act
        ResponseEntity<PlayerGameStats> response = playerStatsController.logPlayerStats(1L, statsDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getPlayerStats_shouldReturnPlayerStatsList() throws SQLException {
        // Arrange
        List<PlayerGameStats> statsList = Arrays.asList(savedStats);
        when(playerStatsService.getPlayerGameStats(1L)).thenReturn(statsList);

        // Act
        ResponseEntity<List<PlayerGameStats>> response = playerStatsController.getPlayerStats(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(statsList, response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getPlayerStats_shouldReturnNotFoundStatus_whenPlayerNotFound() throws SQLException {
        // Arrange
        when(playerStatsService.getPlayerGameStats(1L))
            .thenThrow(new ResourceNotFoundException("Player not found"));

        // Act
        ResponseEntity<List<PlayerGameStats>> response = playerStatsController.getPlayerStats(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getPlayerSeasonStats_shouldReturnPlayerAggregateStats() throws SQLException {
        // Arrange
        PlayerAggregateStatsDTO aggregateStats = new PlayerAggregateStatsDTO();
        aggregateStats.setPlayerId(1L);
        aggregateStats.setFirstName("LeBron");
        aggregateStats.setLastName("James");
        aggregateStats.setAveragePoints(25.0);
        
        when(aggregationService.getPlayerSeasonStats(1L)).thenReturn(aggregateStats);

        // Act
        ResponseEntity<PlayerAggregateStatsDTO> response = playerStatsController.getPlayerSeasonStats(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(aggregateStats, response.getBody());
    }

    @Test
    void getPlayerSeasonStats_shouldReturnNotFoundStatus_whenPlayerNotFound() throws SQLException {
        // Arrange
        when(aggregationService.getPlayerSeasonStats(1L))
            .thenThrow(new ResourceNotFoundException("Player not found"));

        // Act
        ResponseEntity<PlayerAggregateStatsDTO> response = playerStatsController.getPlayerSeasonStats(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}