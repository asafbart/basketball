package com.nba.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nba.stats.dto.PlayerAggregateStatsDTO;
import com.nba.stats.dto.PlayerGameStatsDTO;
import com.nba.stats.dto.TeamAggregateStatsDTO;
import com.nba.stats.exception.InvalidStatsException;
import com.nba.stats.exception.ResourceNotFoundException;
import com.nba.stats.model.Game;
import com.nba.stats.model.Player;
import com.nba.stats.model.PlayerGameStats;
import com.nba.stats.model.Team;
import com.nba.stats.service.CacheService;
import com.nba.stats.service.PlayerGameStatsService;
import com.nba.stats.service.StatsAggregationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
    PlayerStatsController.class,
    GameStatsController.class,
    TeamStatsController.class
})
public class APIControllersMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlayerGameStatsService playerStatsService;

    @MockBean
    private StatsAggregationService aggregationService;
    
    @MockBean
    private CacheService cacheService;

    private PlayerGameStatsDTO validStatsDTO;
    private PlayerGameStats savedStats;
    private Team lakers;
    private Player lebron;
    private Game game;
    private PlayerAggregateStatsDTO aggregateStats;
    private TeamAggregateStatsDTO teamStats;

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
        
        savedStats = new PlayerGameStats();
        savedStats.setId(1L);
        savedStats.setPlayer(lebron);
        savedStats.setGame(game);
        savedStats.setPoints(30);
        savedStats.setRebounds(10);
        savedStats.setAssists(8);
        savedStats.setSteals(2);
        savedStats.setBlocks(1);
        savedStats.setFouls(2);
        savedStats.setTurnovers(3);
        savedStats.setMinutesPlayed(35.5);
        
        aggregateStats = new PlayerAggregateStatsDTO();
        aggregateStats.setPlayerId(1L);
        aggregateStats.setFirstName("LeBron");
        aggregateStats.setLastName("James");
        aggregateStats.setTeamName("Lakers");
        aggregateStats.setGamesPlayed(10);
        aggregateStats.setAveragePoints(27.5);
        aggregateStats.setAverageRebounds(7.3);
        aggregateStats.setAverageAssists(8.1);
        
        teamStats = new TeamAggregateStatsDTO();
        teamStats.setTeamId(1L);
        teamStats.setTeamName("Lakers");
        teamStats.setCity("Los Angeles");
        teamStats.setNumberOfPlayers(12);
        teamStats.setGamesPlayed(20);
        teamStats.setAveragePoints(110.5);
        teamStats.setAverageRebounds(44.2);
        teamStats.setAverageAssists(25.1);
    }
    
    // TEST ENDPOINT: POST /api/players/{playerId}/stats
    @Test
    void logPlayerStats_shouldReturnCreatedStatus_andCorrectData() throws Exception {
        // Arrange
        when(playerStatsService.logPlayerStats(any(PlayerGameStatsDTO.class))).thenReturn(savedStats);
        doNothing().when(aggregationService).invalidateStatsCache(anyLong(), anyLong());
        
        // Act & Assert
        mockMvc.perform(post("/api/players/1/stats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validStatsDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.points", is(30)))
                .andExpect(jsonPath("$.rebounds", is(10)))
                .andExpect(jsonPath("$.assists", is(8)))
                .andExpect(jsonPath("$.player.firstName", is("LeBron")))
                .andExpect(jsonPath("$.player.lastName", is("James")));
                
        // Verify that the playerId is set correctly on the DTO and cache invalidation is called
        verify(playerStatsService).logPlayerStats(argThat(dto -> dto.getPlayerId() == 1L));
        verify(aggregationService).invalidateStatsCache(eq(1L), eq(1L));
    }
    
    @Test
    void logPlayerStats_shouldReturnNotFound_whenPlayerNotFound() throws Exception {
        // Arrange
        when(playerStatsService.logPlayerStats(any(PlayerGameStatsDTO.class)))
            .thenThrow(new ResourceNotFoundException("Player not found"));
        
        // Act & Assert
        mockMvc.perform(post("/api/players/999/stats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validStatsDTO)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void logPlayerStats_shouldReturnBadRequest_whenInvalidStats() throws Exception {
        // Arrange
        when(playerStatsService.logPlayerStats(any(PlayerGameStatsDTO.class)))
            .thenThrow(new InvalidStatsException("Invalid stats"));
        
        // Act & Assert
        mockMvc.perform(post("/api/players/1/stats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validStatsDTO)))
                .andExpect(status().isBadRequest());
    }
    
    // TEST ENDPOINT: GET /api/players/{playerId}/stats
    @Test
    void getPlayerStats_shouldReturnCorrectData() throws Exception {
        // Arrange
        List<PlayerGameStats> statsList = List.of(savedStats);
        when(playerStatsService.getPlayerGameStats(1L)).thenReturn(statsList);
        
        // Act & Assert
        mockMvc.perform(get("/api/players/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].points", is(30)))
                .andExpect(jsonPath("$[0].rebounds", is(10)))
                .andExpect(jsonPath("$[0].player.firstName", is("LeBron")));
    }
    
    @Test
    void getPlayerStats_shouldReturnNotFound_whenPlayerNotFound() throws Exception {
        // Arrange
        when(playerStatsService.getPlayerGameStats(999L))
            .thenThrow(new ResourceNotFoundException("Player not found"));
        
        // Act & Assert
        mockMvc.perform(get("/api/players/999/stats"))
                .andExpect(status().isNotFound());
    }
    
    // TEST ENDPOINT: GET /api/players/{playerId}/season-stats
    @Test
    void getPlayerSeasonStats_shouldReturnCorrectData() throws Exception {
        // Arrange
        when(aggregationService.getPlayerSeasonStats(1L)).thenReturn(aggregateStats);
        
        // Act & Assert
        mockMvc.perform(get("/api/players/1/season-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId", is(1)))
                .andExpect(jsonPath("$.firstName", is("LeBron")))
                .andExpect(jsonPath("$.lastName", is("James")))
                .andExpect(jsonPath("$.teamName", is("Lakers")))
                .andExpect(jsonPath("$.gamesPlayed", is(10)))
                .andExpect(jsonPath("$.averagePoints", is(27.5)));
    }
    
    // TEST ENDPOINT: POST /api/games/{gameId}/stats
    @Test
    void logGameStats_shouldReturnCreatedStatus_andCorrectData() throws Exception {
        // Arrange
        when(playerStatsService.logPlayerStats(any(PlayerGameStatsDTO.class))).thenReturn(savedStats);
        doNothing().when(aggregationService).invalidateStatsCache(anyLong(), anyLong());
        
        // Act & Assert
        mockMvc.perform(post("/api/games/1/stats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validStatsDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.points", is(30)))
                .andExpect(jsonPath("$.player.firstName", is("LeBron")));
                
        // Verify that the gameId is set correctly on the DTO
        verify(playerStatsService).logPlayerStats(argThat(dto -> dto.getGameId() == 1L));
        verify(aggregationService).invalidateStatsCache(eq(1L), eq(1L));
    }
    
    // TEST ENDPOINT: POST /api/games/{gameId}/stats/batch
    @Test
    void logMultipleGameStats_shouldReturnSuccess() throws Exception {
        // Arrange - create a list of stat DTOs
        PlayerGameStatsDTO statsDTO1 = new PlayerGameStatsDTO();
        statsDTO1.setPlayerId(1L);
        statsDTO1.setPoints(30);
        statsDTO1.setRebounds(5);
        statsDTO1.setAssists(4);
        statsDTO1.setSteals(1);
        statsDTO1.setBlocks(0);
        statsDTO1.setFouls(2);
        statsDTO1.setTurnovers(1);
        statsDTO1.setMinutesPlayed(20.0);
        
        PlayerGameStatsDTO statsDTO2 = new PlayerGameStatsDTO();
        statsDTO2.setPlayerId(2L);
        statsDTO2.setPoints(22);
        statsDTO2.setRebounds(10);
        statsDTO2.setAssists(2);
        statsDTO2.setSteals(0);
        statsDTO2.setBlocks(3);
        statsDTO2.setFouls(1);
        statsDTO2.setTurnovers(0);
        statsDTO2.setMinutesPlayed(25.0);
        
        List<PlayerGameStatsDTO> statsDTOList = Arrays.asList(statsDTO1, statsDTO2);
        
        // Create mock returning different players with different teams
        Player davis = new Player(2L, "Anthony", "Davis", "F/C", lakers);
        
        PlayerGameStats savedStats1 = new PlayerGameStats();
        savedStats1.setPlayer(lebron);
        savedStats1.setGame(game);
        
        PlayerGameStats savedStats2 = new PlayerGameStats();
        savedStats2.setPlayer(davis);
        savedStats2.setGame(game);
        
        // Use any() instead of specific matchers to avoid NPE
        when(playerStatsService.logPlayerStats(any(PlayerGameStatsDTO.class)))
            .thenAnswer(invocation -> {
                PlayerGameStatsDTO dto = invocation.getArgument(0);
                if (dto.getPlayerId() == 1L) {
                    return savedStats1;
                } else {
                    return savedStats2;
                }
            });
            
        // Act & Assert
        mockMvc.perform(post("/api/games/1/stats/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statsDTOList)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("Successfully logged stats for 2 players")));
                
        // Verify cache invalidation was called for both players
        verify(aggregationService, times(2)).invalidateStatsCache(anyLong(), anyLong());
    }
    
    // TEST ENDPOINT: GET /api/games/{gameId}/stats
    @Test
    void getGameStats_shouldReturnCorrectData() throws Exception {
        // Arrange
        List<PlayerGameStats> statsList = List.of(savedStats);
        when(playerStatsService.getGameStats(1L)).thenReturn(statsList);
        
        // Act & Assert
        mockMvc.perform(get("/api/games/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].points", is(30)))
                .andExpect(jsonPath("$[0].player.firstName", is("LeBron")));
    }
    
    // TEST ENDPOINT: GET /api/teams/{teamId}/season-stats
    @Test
    void getTeamSeasonStats_shouldReturnCorrectData() throws Exception {
        // Arrange
        when(aggregationService.getTeamSeasonStats(1L)).thenReturn(teamStats);
        
        // Act & Assert
        mockMvc.perform(get("/api/teams/1/season-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId", is(1)))
                .andExpect(jsonPath("$.teamName", is("Lakers")))
                .andExpect(jsonPath("$.city", is("Los Angeles")))
                .andExpect(jsonPath("$.numberOfPlayers", is(12)))
                .andExpect(jsonPath("$.gamesPlayed", is(20)))
                .andExpect(jsonPath("$.averagePoints", is(110.5)));
    }
    
    // TEST ENDPOINT: GET /api/teams/season-stats
    @Test
    void getAllTeamSeasonStats_shouldReturnCorrectData() throws Exception {
        // Arrange
        TeamAggregateStatsDTO celticsStats = new TeamAggregateStatsDTO();
        celticsStats.setTeamId(2L);
        celticsStats.setTeamName("Celtics");
        
        List<TeamAggregateStatsDTO> allTeamStats = Arrays.asList(teamStats, celticsStats);
        when(aggregationService.getAllTeamSeasonStats()).thenReturn(allTeamStats);
        
        // Act & Assert
        mockMvc.perform(get("/api/teams/season-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].teamName", is("Lakers")))
                .andExpect(jsonPath("$[1].teamName", is("Celtics")));
    }
    
    // Error handling tests
    @Test
    void allEndpoints_shouldReturnServerError_onSQLException() throws Exception {
        // Arrange
        when(playerStatsService.logPlayerStats(any(PlayerGameStatsDTO.class)))
            .thenThrow(new SQLException("Database error"));
        
        // Act & Assert
        mockMvc.perform(post("/api/players/1/stats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validStatsDTO)))
                .andExpect(status().isInternalServerError());
    }
}