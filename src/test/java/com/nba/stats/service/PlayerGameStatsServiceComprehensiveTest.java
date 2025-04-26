package com.nba.stats.service;

import com.nba.stats.dto.PlayerGameStatsDTO;
import com.nba.stats.exception.InvalidStatsException;
import com.nba.stats.exception.ResourceNotFoundException;
import com.nba.stats.model.Game;
import com.nba.stats.model.Player;
import com.nba.stats.model.PlayerGameStats;
import com.nba.stats.model.Team;
import com.nba.stats.repository.GameRepository;
import com.nba.stats.repository.PlayerGameStatsRepository;
import com.nba.stats.repository.PlayerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerGameStatsServiceComprehensiveTest {

    @Mock
    private PlayerGameStatsRepository statsRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private PlayerGameStatsService statsService;

    private PlayerGameStatsDTO validStatsDTO;
    private Team lakers;
    private Player lebron;
    private Game game;

    @BeforeEach
    void setUp() {
        // Setup test data
        lakers = new Team(1L, "Lakers", "Los Angeles");
        lebron = new Player(1L, "LeBron", "James", "F", lakers);
        game = new Game(1L, LocalDate.of(2025, 1, 15), lakers, null, 120, 115);
        
        validStatsDTO = new PlayerGameStatsDTO();
        validStatsDTO.setPlayerId(1L);
        validStatsDTO.setGameId(1L);
        validStatsDTO.setPoints(30);
        validStatsDTO.setRebounds(10);
        validStatsDTO.setAssists(8);
        validStatsDTO.setSteals(2);
        validStatsDTO.setBlocks(1);
        validStatsDTO.setFouls(2);
        validStatsDTO.setTurnovers(3);
        validStatsDTO.setMinutesPlayed(35.5);
    }

    @Test
    void logPlayerStats_shouldSaveAndReturnPlayerStats_whenValid() throws SQLException {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(lebron));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        
        PlayerGameStats statsToSave = new PlayerGameStats();
        statsToSave.setPlayer(lebron);
        statsToSave.setGame(game);
        statsToSave.setPoints(30);
        
        PlayerGameStats savedStats = new PlayerGameStats();
        savedStats.setId(1L);
        savedStats.setPlayer(lebron);
        savedStats.setGame(game);
        savedStats.setPoints(30);
        savedStats.setRebounds(10);
        savedStats.setAssists(8);
        
        when(statsRepository.save(any(PlayerGameStats.class))).thenReturn(savedStats);
        
        // Act
        PlayerGameStats result = statsService.logPlayerStats(validStatsDTO);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(lebron, result.getPlayer());
        assertEquals(30, result.getPoints());
        
        // Verify the save was called
        verify(statsRepository).save(any(PlayerGameStats.class));
    }
    
    @Test
    void logPlayerStats_shouldThrowResourceNotFoundException_whenPlayerNotFound()
        throws SQLException {
        // Arrange
        when(playerRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            statsService.logPlayerStats(validStatsDTO);
        });
        
        // Verify no save was attempted
        verify(statsRepository, never()).save(any(PlayerGameStats.class));
    }
    
    @Test
    void logPlayerStats_shouldThrowResourceNotFoundException_whenGameNotFound()
        throws SQLException {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(lebron));
        when(gameRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            statsService.logPlayerStats(validStatsDTO);
        });
        
        // Verify no save was attempted
        verify(statsRepository, never()).save(any(PlayerGameStats.class));
    }
    
    @Test
    void logPlayerStats_shouldThrowInvalidStatsException_whenStatsInvalid() throws SQLException {
        // Arrange
//        when(playerRepository.findById(1L)).thenReturn(Optional.of(lebron));
//        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        
        // Create invalid stats (negative points)
        PlayerGameStatsDTO invalidStatsDTO = new PlayerGameStatsDTO();
        invalidStatsDTO.setPlayerId(1L);
        invalidStatsDTO.setGameId(1L);
        invalidStatsDTO.setPoints(-10); // Negative points is invalid
        
        // Act & Assert
        assertThrows(InvalidStatsException.class, () -> {
            statsService.logPlayerStats(invalidStatsDTO);
        });
        
        // Verify no save was attempted
        verify(statsRepository, never()).save(any(PlayerGameStats.class));
    }
    
    @Test
    void getPlayerGameStats_shouldReturnPlayerStats_whenPlayerExists() throws SQLException {
        // Arrange
        PlayerGameStats stats1 = new PlayerGameStats();
        stats1.setId(1L);
        stats1.setPlayer(lebron);
        stats1.setPoints(30);
        
        PlayerGameStats stats2 = new PlayerGameStats();
        stats2.setId(2L);
        stats2.setPlayer(lebron);
        stats2.setPoints(24);
        
        List<PlayerGameStats> mockStats = Arrays.asList(stats1, stats2);
        
        when(playerRepository.findById(1L)).thenReturn(Optional.of(lebron));
        when(statsRepository.findByPlayerId(1L)).thenReturn(mockStats);
        
        // Act
        List<PlayerGameStats> result = statsService.getPlayerGameStats(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(30, result.get(0).getPoints());
        assertEquals(24, result.get(1).getPoints());
    }
    
    @Test
    void getPlayerGameStats_shouldThrowResourceNotFoundException_whenPlayerNotFound()
        throws SQLException {
        // Arrange
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            statsService.getPlayerGameStats(999L);
        });
    }
    
    @Test
    void getGameStats_shouldReturnGameStats_whenGameExists() throws SQLException {
        // Arrange
        PlayerGameStats stats1 = new PlayerGameStats();
        stats1.setId(1L);
        stats1.setPlayer(lebron);
        stats1.setGame(game);
        stats1.setPoints(30);
        
        Player davis = new Player(2L, "Anthony", "Davis", "F/C", lakers);
        
        PlayerGameStats stats2 = new PlayerGameStats();
        stats2.setId(2L);
        stats2.setPlayer(davis);
        stats2.setGame(game);
        stats2.setPoints(22);
        
        List<PlayerGameStats> mockStats = Arrays.asList(stats1, stats2);
        
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(statsRepository.findByGameId(1L)).thenReturn(mockStats);
        
        // Act
        List<PlayerGameStats> result = statsService.getGameStats(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(30, result.get(0).getPoints());
        assertEquals(22, result.get(1).getPoints());
        assertEquals("LeBron", result.get(0).getPlayer().getFirstName());
        assertEquals("Anthony", result.get(1).getPlayer().getFirstName());
    }
    
    @Test
    void getGameStats_shouldThrowResourceNotFoundException_whenGameNotFound() throws SQLException {
        // Arrange
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            statsService.getGameStats(999L);
        });
    }
    
    @Test
    void validateStats_shouldNotThrowException_whenStatsValid() {
        // Act & Assert - no exception expected
        assertDoesNotThrow(() -> {
            statsService.validateStats(validStatsDTO);
        });
    }
    
    @Test
    void validateStats_shouldThrowInvalidStatsException_whenPointsNegative() {
        // Arrange
        PlayerGameStatsDTO invalidStats = new PlayerGameStatsDTO();
        invalidStats.setPoints(-1);
        // We need to set the other fields to valid values to avoid failures unrelated to points
        invalidStats.setRebounds(5);
        invalidStats.setAssists(2);
        invalidStats.setSteals(1);
        invalidStats.setBlocks(0);
        invalidStats.setFouls(2);
        invalidStats.setTurnovers(1);
        invalidStats.setMinutesPlayed(20.0);
        
        // Act & Assert
        assertThrows(InvalidStatsException.class, () -> {
            statsService.validateStats(invalidStats);
        });
    }
    
    @Test
    void validateStats_shouldThrowInvalidStatsException_whenMinutesPlayedNegative() {
        // Arrange
        PlayerGameStatsDTO invalidStats = new PlayerGameStatsDTO();
        // Set all fields to valid values
        invalidStats.setPoints(10);
        invalidStats.setRebounds(5);
        invalidStats.setAssists(2);
        invalidStats.setSteals(1);
        invalidStats.setBlocks(0);
        invalidStats.setFouls(2);
        invalidStats.setTurnovers(1);
        // Except minutes played, which is invalid
        invalidStats.setMinutesPlayed(-1.0);
        
        // Act & Assert
        assertThrows(InvalidStatsException.class, () -> {
            statsService.validateStats(invalidStats);
        });
    }
    
    @Test
    void validateStats_shouldThrowInvalidStatsException_whenMinutesPlayedTooHigh() {
        // Arrange
        PlayerGameStatsDTO invalidStats = new PlayerGameStatsDTO();
        // Set all fields to valid values
        invalidStats.setPoints(10);
        invalidStats.setRebounds(5);
        invalidStats.setAssists(2);
        invalidStats.setSteals(1);
        invalidStats.setBlocks(0);
        invalidStats.setFouls(2);
        invalidStats.setTurnovers(1);
        // Except minutes played, which is too high
        invalidStats.setMinutesPlayed(60.1); // More than 48 mins in a game
        
        // Act & Assert
        assertThrows(InvalidStatsException.class, () -> {
            statsService.validateStats(invalidStats);
        });
    }
    
    @Test
    void validateStats_shouldThrowInvalidStatsException_whenFieldIsNull() {
        // Arrange - create stats with null fields one by one to test each validation
        
        // Test with null points
        PlayerGameStatsDTO statsWithNullPoints = new PlayerGameStatsDTO();
        // Set all fields except points
        statsWithNullPoints.setRebounds(5);
        statsWithNullPoints.setAssists(2);
        statsWithNullPoints.setSteals(1);
        statsWithNullPoints.setBlocks(0);
        statsWithNullPoints.setFouls(2);
        statsWithNullPoints.setTurnovers(1);
        statsWithNullPoints.setMinutesPlayed(20.0);
        
        assertThrows(InvalidStatsException.class, () -> {
            statsService.validateStats(statsWithNullPoints);
        });
        
        // Test with null rebounds
        PlayerGameStatsDTO statsWithNullRebounds = new PlayerGameStatsDTO();
        statsWithNullRebounds.setPoints(10);
        // rebounds is null
        statsWithNullRebounds.setAssists(2);
        statsWithNullRebounds.setSteals(1);
        statsWithNullRebounds.setBlocks(0);
        statsWithNullRebounds.setFouls(2);
        statsWithNullRebounds.setTurnovers(1);
        statsWithNullRebounds.setMinutesPlayed(20.0);
        
        assertThrows(InvalidStatsException.class, () -> {
            statsService.validateStats(statsWithNullRebounds);
        });
        
        // Test with null minutes played (last field in validation)
        PlayerGameStatsDTO statsWithNullMinutes = new PlayerGameStatsDTO();
        statsWithNullMinutes.setPoints(10);
        statsWithNullMinutes.setRebounds(5);
        statsWithNullMinutes.setAssists(2);
        statsWithNullMinutes.setSteals(1);
        statsWithNullMinutes.setBlocks(0);
        statsWithNullMinutes.setFouls(2);
        statsWithNullMinutes.setTurnovers(1);
        // minutes played is null
        
        assertThrows(InvalidStatsException.class, () -> {
            statsService.validateStats(statsWithNullMinutes);
        });
    }
}