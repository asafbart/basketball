package com.nba.stats.service;

import com.nba.stats.dto.PlayerAggregateStatsDTO;
import com.nba.stats.dto.TeamAggregateStatsDTO;
import com.nba.stats.exception.ResourceNotFoundException;
import com.nba.stats.model.Game;
import com.nba.stats.model.Player;
import com.nba.stats.model.PlayerGameStats;
import com.nba.stats.model.Season;
import com.nba.stats.model.Team;
import com.nba.stats.repository.PlayerGameStatsRepository;
import com.nba.stats.repository.PlayerRepository;
import com.nba.stats.repository.SeasonRepository;
import com.nba.stats.repository.TeamRepository;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

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
public class StatsAggregationServiceTest {

    @Mock
    private PlayerGameStatsRepository statsRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private SeasonRepository seasonRepository;
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private CacheService cacheService;

    @InjectMocks
    private StatsAggregationService aggregationService;

    private Team lakers;
    private Team celtics;
    private Player lebron;
    private Player davis;
    private Game game1;
    private Game game2;
    private PlayerGameStats lebronGame1Stats;
    private PlayerGameStats lebronGame2Stats;
    private PlayerGameStats davisGame1Stats;
    private Season currentSeason;

    @BeforeEach
    void setUp() {
        // Test data setup
        lakers = new Team(1L, "Lakers", "Los Angeles");
        celtics = new Team(2L, "Celtics", "Boston");
        
        lebron = new Player(1L, "LeBron", "James", "F", lakers);
        davis = new Player(2L, "Anthony", "Davis", "F/C", lakers);
        
        currentSeason = new Season(1L, 2025, LocalDate.of(2024, 10, 22), LocalDate.of(2025, 6, 19));
        
        game1 = new Game(1L, LocalDate.of(2025, 1, 15), lakers, celtics, 120, 115);
        game2 = new Game(2L, LocalDate.of(2025, 1, 20), celtics, lakers, 110, 125);
        
        // LeBron's stats for game 1
        lebronGame1Stats = new PlayerGameStats();
        lebronGame1Stats.setId(1L);
        lebronGame1Stats.setPlayer(lebron);
        lebronGame1Stats.setGame(game1);
        lebronGame1Stats.setPoints(30);
        lebronGame1Stats.setRebounds(10);
        lebronGame1Stats.setAssists(8);
        lebronGame1Stats.setSteals(2);
        lebronGame1Stats.setBlocks(1);
        lebronGame1Stats.setFouls(2);
        lebronGame1Stats.setTurnovers(3);
        lebronGame1Stats.setMinutesPlayed(35.5);
        
        // LeBron's stats for game 2
        lebronGame2Stats = new PlayerGameStats();
        lebronGame2Stats.setId(2L);
        lebronGame2Stats.setPlayer(lebron);
        lebronGame2Stats.setGame(game2);
        lebronGame2Stats.setPoints(24);
        lebronGame2Stats.setRebounds(7);
        lebronGame2Stats.setAssists(11);
        lebronGame2Stats.setSteals(1);
        lebronGame2Stats.setBlocks(0);
        lebronGame2Stats.setFouls(3);
        lebronGame2Stats.setTurnovers(2);
        lebronGame2Stats.setMinutesPlayed(32.0);
        
        // Davis's stats for game 1
        davisGame1Stats = new PlayerGameStats();
        davisGame1Stats.setId(3L);
        davisGame1Stats.setPlayer(davis);
        davisGame1Stats.setGame(game1);
        davisGame1Stats.setPoints(22);
        davisGame1Stats.setRebounds(12);
        davisGame1Stats.setAssists(3);
        davisGame1Stats.setSteals(1);
        davisGame1Stats.setBlocks(4);
        davisGame1Stats.setFouls(1);
        davisGame1Stats.setTurnovers(2);
        davisGame1Stats.setMinutesPlayed(30.5);
    }

    @Test
    void getPlayerSeasonStats_shouldCalculateCorrectAverages() throws SQLException {
        // Arrange
        List<PlayerGameStats> lebronStats = Arrays.asList(lebronGame1Stats, lebronGame2Stats);
        
        when(playerRepository.findById(1L)).thenReturn(Optional.of(lebron));
        when(seasonRepository.findCurrentSeason()).thenReturn(Optional.of(currentSeason));
        when(statsRepository.findByPlayerIdAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(lebronStats);

        // Act
        PlayerAggregateStatsDTO result = aggregationService.getPlayerSeasonStats(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPlayerId());
        assertEquals("LeBron", result.getFirstName());
        assertEquals("James", result.getLastName());
        assertEquals("Lakers", result.getTeamName());
        assertEquals(2, result.getGamesPlayed());
        
        // Check averages
        assertEquals(27.0, result.getAveragePoints(), 0.01); // (30 + 24) / 2
        assertEquals(8.5, result.getAverageRebounds(), 0.01); // (10 + 7) / 2
        assertEquals(9.5, result.getAverageAssists(), 0.01); // (8 + 11) / 2
        assertEquals(1.5, result.getAverageSteals(), 0.01); // (2 + 1) / 2
        assertEquals(0.5, result.getAverageBlocks(), 0.01); // (1 + 0) / 2
        assertEquals(2.5, result.getAverageFouls(), 0.01); // (2 + 3) / 2
        assertEquals(2.5, result.getAverageTurnovers(), 0.01); // (3 + 2) / 2
        assertEquals(33.75, result.getAverageMinutesPlayed(), 0.01); // (35.5 + 32.0) / 2
    }
    
    @Test
    void getPlayerSeasonStats_shouldReturnEmptyStats_whenNoGamesPlayed() throws SQLException {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(lebron));
        when(seasonRepository.findCurrentSeason()).thenReturn(Optional.of(currentSeason));
        when(statsRepository.findByPlayerIdAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        // Act
        PlayerAggregateStatsDTO result = aggregationService.getPlayerSeasonStats(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPlayerId());
        assertEquals("LeBron", result.getFirstName());
        assertEquals("James", result.getLastName());
        assertEquals("Lakers", result.getTeamName());
        assertEquals(0, result.getGamesPlayed());
        assertEquals(0.0, result.getAveragePoints());
    }
    
    @Test
    void getPlayerSeasonStats_shouldThrowResourceNotFoundException_whenPlayerNotFound()
        throws SQLException {
        // Arrange
        when(playerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            aggregationService.getPlayerSeasonStats(1L);
        });
    }
    
    @Test
    void getPlayerSeasonStats_shouldThrowResourceNotFoundException_whenSeasonNotFound()
        throws SQLException {
        // Arrange
        when(playerRepository.findById(1L)).thenReturn(Optional.of(lebron));
        when(seasonRepository.findCurrentSeason()).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            aggregationService.getPlayerSeasonStats(1L);
        });
    }
    
    @Test
    void getTeamSeasonStats_shouldCalculateCorrectTeamAverages() throws SQLException {
        // Arrange
        List<PlayerGameStats> teamStats = Arrays.asList(lebronGame1Stats, davisGame1Stats, lebronGame2Stats);
        
        when(teamRepository.findById(1L)).thenReturn(Optional.of(lakers));
        when(seasonRepository.findCurrentSeason()).thenReturn(Optional.of(currentSeason));
        when(statsRepository.findByTeamIdAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(teamStats);

        // Act
        TeamAggregateStatsDTO result = aggregationService.getTeamSeasonStats(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getTeamId());
        assertEquals("Lakers", result.getTeamName());
        assertEquals("Los Angeles", result.getCity());
        assertEquals(2, result.getNumberOfPlayers()); // LeBron and Davis
        assertEquals(2, result.getGamesPlayed()); // Max games played by any player
        
        // Expected averages:
        // LeBron: (Game1 + Game2) / 2 games = [27.0, 8.5, 9.5, 1.5, 0.5, 2.5, 2.5, 33.75]
        // Davis: Game1 / 1 game = [22.0, 12.0, 3.0, 1.0, 4.0, 1.0, 2.0, 30.5]
        // Team average: (LeBron avg + Davis avg) / 2 players
        
        assertEquals(24.5, result.getAveragePoints(), 0.01); // (27.0 + 22.0) / 2
        assertEquals(10.25, result.getAverageRebounds(), 0.01); // (8.5 + 12.0) / 2
        assertEquals(6.25, result.getAverageAssists(), 0.01); // (9.5 + 3.0) / 2
        assertEquals(1.25, result.getAverageSteals(), 0.01); // (1.5 + 1.0) / 2
        assertEquals(2.25, result.getAverageBlocks(), 0.01); // (0.5 + 4.0) / 2
        assertEquals(1.75, result.getAverageFouls(), 0.01); // (2.5 + 1.0) / 2
        assertEquals(2.25, result.getAverageTurnovers(), 0.01); // (2.5 + 2.0) / 2
        assertEquals(32.125, result.getAverageMinutesPlayed(), 0.01); // (33.75 + 30.5) / 2
    }
    
    @Test
    void getTeamSeasonStats_shouldReturnEmptyStats_whenNoGamesPlayed() throws SQLException {
        // Arrange
        when(teamRepository.findById(1L)).thenReturn(Optional.of(lakers));
        when(seasonRepository.findCurrentSeason()).thenReturn(Optional.of(currentSeason));
        when(statsRepository.findByTeamIdAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of());

        // Act
        TeamAggregateStatsDTO result = aggregationService.getTeamSeasonStats(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getTeamId());
        assertEquals("Lakers", result.getTeamName());
        assertEquals("Los Angeles", result.getCity());
        assertEquals(0, result.getNumberOfPlayers());
        assertEquals(0, result.getGamesPlayed());
        assertEquals(0.0, result.getAveragePoints());
    }
    
    @Test
    void getAllTeamSeasonStats_shouldReturnAllTeamStats() throws SQLException {
        // Arrange
        List<Team> teams = Arrays.asList(lakers, celtics);
        TeamAggregateStatsDTO lakersStats = new TeamAggregateStatsDTO();
        lakersStats.setTeamId(1L);
        TeamAggregateStatsDTO celticsStats = new TeamAggregateStatsDTO();
        celticsStats.setTeamId(2L);
        
        when(teamRepository.findAll()).thenReturn(teams);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(lakers));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(celtics));
        when(seasonRepository.findCurrentSeason()).thenReturn(Optional.of(currentSeason));
        // Mock empty stats lists for simplicity
        when(statsRepository.findByTeamIdAndDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of());

        // Act
        List<TeamAggregateStatsDTO> result = aggregationService.getAllTeamSeasonStats();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }
    
    @Test
    void invalidateStatsCache_shouldCallSpecificCacheInvalidationMethods() {
        // Act - this calls multiple cache methods through CacheService
        aggregationService.invalidateStatsCache(1L, 1L);
        
        // Verify that the cacheService.invalidateStatsCache method was called with correct parameters
        verify(cacheService, times(1)).invalidateStatsCache(1L, 1L);
    }
}