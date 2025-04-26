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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerGameStatsServiceTest {

    @Mock
    private PlayerGameStatsRepository statsRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private PlayerGameStatsService playerGameStatsService;

    private Team team;
    private Player player;
    private Game game;
    private PlayerGameStats playerGameStats;
    private PlayerGameStatsDTO statsDTO;

    @BeforeEach
    void setUp() {
        // Test data setup
        team = new Team(1L, "Lakers", "Los Angeles");
        player = new Player(1L, "LeBron", "James", "F", team);
        game = new Game(1L, LocalDate.of(2025, 3, 15), team, new Team(2L, "Celtics", "Boston"), 120, 115);
        
        playerGameStats = new PlayerGameStats();
        playerGameStats.setId(1L);
        playerGameStats.setPlayer(player);
        playerGameStats.setGame(game);
        playerGameStats.setPoints(30);
        playerGameStats.setRebounds(10);
        playerGameStats.setAssists(8);
        playerGameStats.setSteals(2);
        playerGameStats.setBlocks(1);
        playerGameStats.setFouls(2);
        playerGameStats.setTurnovers(3);
        playerGameStats.setMinutesPlayed(35.5);
        
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
    }

    @Test
    void logPlayerStats_shouldSaveStatsSuccessfully() throws SQLException {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(statsRepository.save(any(PlayerGameStats.class))).thenReturn(playerGameStats);

        // Act
        PlayerGameStats result = playerGameStatsService.logPlayerStats(statsDTO);

        // Assert
        assertNotNull(result);
        assertEquals(30, result.getPoints());
        assertEquals(10, result.getRebounds());
        verify(statsRepository, times(1)).save(any(PlayerGameStats.class));
    }

    @Test
    void logPlayerStats_shouldThrowResourceNotFoundException_whenPlayerNotFound()
        throws SQLException {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            playerGameStatsService.logPlayerStats(statsDTO);
        });
        
        verify(statsRepository, never()).save(any(PlayerGameStats.class));
    }

    @Test
    void logPlayerStats_shouldThrowResourceNotFoundException_whenGameNotFound()
        throws SQLException {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            playerGameStatsService.logPlayerStats(statsDTO);
        });
        
        verify(statsRepository, never()).save(any(PlayerGameStats.class));
    }

    @Test
    void logPlayerStats_shouldThrowInvalidStatsException_whenFoulsExceedMax() throws SQLException {
        // Arrange
        statsDTO.setFouls(7); // More than max 6 fouls
        
        // Act & Assert
        assertThrows(InvalidStatsException.class, () -> {
            playerGameStatsService.logPlayerStats(statsDTO);
        });
        
        verify(playerRepository, never()).findById(any());
        verify(gameRepository, never()).findById(any());
        verify(statsRepository, never()).save(any());
    }

    @Test
    void logPlayerStats_shouldThrowInvalidStatsException_whenMinutesPlayedExceedMax()
        throws SQLException {
        // Arrange
        statsDTO.setMinutesPlayed(50.0); // More than max 48 minutes
        
        // Act & Assert
        assertThrows(InvalidStatsException.class, () -> {
            playerGameStatsService.logPlayerStats(statsDTO);
        });
        
        verify(playerRepository, never()).findById(any());
        verify(gameRepository, never()).findById(any());
        verify(statsRepository, never()).save(any());
    }

    @Test
    void getPlayerGameStats_shouldReturnPlayerStats() throws SQLException {
        // Arrange
        List<PlayerGameStats> statsList = Arrays.asList(playerGameStats);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(statsRepository.findByPlayerId(1L)).thenReturn(statsList);

        // Act
        List<PlayerGameStats> result = playerGameStatsService.getPlayerGameStats(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(30, result.get(0).getPoints());
        verify(statsRepository, times(1)).findByPlayerId(1L);
    }

    @Test
    void getPlayerGameStats_shouldThrowResourceNotFoundException_whenPlayerNotFound()
        throws SQLException {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            playerGameStatsService.getPlayerGameStats(1L);
        });
        
        verify(statsRepository, never()).findByPlayerId(any());
    }

    @Test
    void getGameStats_shouldReturnGameStats() throws SQLException {
        // Arrange
        List<PlayerGameStats> statsList = Arrays.asList(playerGameStats);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(statsRepository.findByGameId(1L)).thenReturn(statsList);

        // Act
        List<PlayerGameStats> result = playerGameStatsService.getGameStats(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(30, result.get(0).getPoints());
        verify(statsRepository, times(1)).findByGameId(1L);
    }

    @Test
    void getGameStats_shouldThrowResourceNotFoundException_whenGameNotFound() throws SQLException {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            playerGameStatsService.getGameStats(1L);
        });
        
        verify(statsRepository, never()).findByGameId(any());
    }
}