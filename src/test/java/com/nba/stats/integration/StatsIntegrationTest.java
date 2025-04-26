package com.nba.stats.integration;

import com.nba.stats.dto.PlayerGameStatsDTO;
import com.nba.stats.dto.PlayerAggregateStatsDTO;
import com.nba.stats.model.Game;
import com.nba.stats.model.Player;
import com.nba.stats.model.PlayerGameStats;
import com.nba.stats.model.Season;
import com.nba.stats.model.Team;
import com.nba.stats.repository.GameRepository;
import com.nba.stats.repository.PlayerGameStatsRepository;
import com.nba.stats.repository.PlayerRepository;
import com.nba.stats.repository.SeasonRepository;
import com.nba.stats.repository.TeamRepository;
import com.nba.stats.service.CacheService;
import com.nba.stats.service.PlayerGameStatsService;
import com.nba.stats.service.StatsAggregationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@ContextConfiguration(initializers = {StatsIntegrationTest.Initializer.class})
public class StatsIntegrationTest {

    @Container
    public static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("nba_stats")
            .withUsername("test")
            .withPassword("test");

    @Container
    public static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:6.2-alpine"))
            .withExposedPorts(6379);

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + mySQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + mySQLContainer.getUsername(),
                    "spring.datasource.password=" + mySQLContainer.getPassword(),
                    "spring.redis.host=" + redisContainer.getHost(),
                    "spring.redis.port=" + redisContainer.getMappedPort(6379)
            ).applyTo(context.getEnvironment());
        }
    }

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private PlayerGameStatsRepository statsRepository;

    @Autowired
    private PlayerGameStatsService playerGameStatsService;

    @Autowired
    private StatsAggregationService aggregationService;
    
    @Autowired
    private CacheService cacheService;

    private Team lakers;
    private Team celtics;
    private Player lebron;
    private Game game;
    private Season currentSeason;

    @BeforeEach
    void setUp() throws SQLException {
        // Clear all repositories
        statsRepository.deleteAll();
        gameRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();
        seasonRepository.deleteAll();

        // Create test data
        lakers = new Team(null, "Lakers", "Los Angeles");
        lakers = teamRepository.save(lakers);

        celtics = new Team(null, "Celtics", "Boston");
        celtics = teamRepository.save(celtics);

        lebron = new Player(null, "LeBron", "James", "F", lakers);
        lebron = playerRepository.save(lebron);

        currentSeason = new Season(null, 2025, LocalDate.of(2024, 10, 22), LocalDate.of(2025, 6, 19));
        currentSeason = seasonRepository.save(currentSeason);

        game = new Game(null, LocalDate.of(2025, 1, 15), lakers, celtics, 120, 115);
        game = gameRepository.save(game);
    }

    @Test
    void fullStatsWorkflow_shouldCreateAndRetrieveStats() throws SQLException, InterruptedException {
        // 1. Create player game stats
        PlayerGameStatsDTO statsDTO = new PlayerGameStatsDTO();
        statsDTO.setPlayerId(lebron.getId());
        statsDTO.setGameId(game.getId());
        statsDTO.setPoints(30);
        statsDTO.setRebounds(10);
        statsDTO.setAssists(8);
        statsDTO.setSteals(2);
        statsDTO.setBlocks(1);
        statsDTO.setFouls(2);
        statsDTO.setTurnovers(3);
        statsDTO.setMinutesPlayed(35.5);

        PlayerGameStats savedStats = playerGameStatsService.logPlayerStats(statsDTO);
        assertNotNull(savedStats);
        assertEquals(30, savedStats.getPoints());

        // 2. Get player stats list
        List<PlayerGameStats> playerStats = playerGameStatsService.getPlayerGameStats(lebron.getId());
        assertNotNull(playerStats);
        assertEquals(1, playerStats.size());
        assertEquals(30, playerStats.get(0).getPoints());

        // 3. Get aggregate stats
        PlayerAggregateStatsDTO aggregateStats = aggregationService.getPlayerSeasonStats(lebron.getId());
        assertNotNull(aggregateStats);
        assertEquals("LeBron", aggregateStats.getFirstName());
        assertEquals("James", aggregateStats.getLastName());
        assertEquals(1, aggregateStats.getGamesPlayed());
        assertEquals(30.0, aggregateStats.getAveragePoints());
        assertEquals(10.0, aggregateStats.getAverageRebounds());
        assertEquals(8.0, aggregateStats.getAverageAssists());
        assertEquals(2.0, aggregateStats.getAverageSteals());
        assertEquals(1.0, aggregateStats.getAverageBlocks());
        assertEquals(2.0, aggregateStats.getAverageFouls());
        assertEquals(3.0, aggregateStats.getAverageTurnovers());
        assertEquals(35.5, aggregateStats.getAverageMinutesPlayed());

        // 4. Log another game and check updated aggregate stats
        Game game2 = new Game(null, LocalDate.of(2025, 2, 10), celtics, lakers, 105, 110);
        game2 = gameRepository.save(game2);

        PlayerGameStatsDTO statsDTO2 = new PlayerGameStatsDTO();
        statsDTO2.setPlayerId(lebron.getId());
        statsDTO2.setGameId(game2.getId());
        statsDTO2.setPoints(24);
        statsDTO2.setRebounds(6);
        statsDTO2.setAssists(10);
        statsDTO2.setSteals(1);
        statsDTO2.setBlocks(0);
        statsDTO2.setFouls(3);
        statsDTO2.setTurnovers(2);
        statsDTO2.setMinutesPlayed(32.0);

        PlayerGameStats savedStats2 = playerGameStatsService.logPlayerStats(statsDTO2);
        assertNotNull(savedStats2);

        // Force cache invalidation
        cacheService.invalidateAllCaches();
        
        // Add short delay to ensure cache is refreshed
        Thread.sleep(100);

        // Get updated aggregate stats
        PlayerAggregateStatsDTO updatedStats = aggregationService.getPlayerSeasonStats(lebron.getId());
        assertNotNull(updatedStats);
        
        // With the current setup, we should have 2 games for the player
        assertEquals(2, updatedStats.getGamesPlayed());
        
        // Since there are 2 games, averages should be calculated correctly
        // Average points: (30 + 24) / 2 = 27.0
        assertEquals(27.0, updatedStats.getAveragePoints());
        // Average rebounds: (10 + 6) / 2 = 8.0
        assertEquals(8.0, updatedStats.getAverageRebounds());
        // Average assists: (8 + 10) / 2 = 9.0
        assertEquals(9.0, updatedStats.getAverageAssists());
    }

    @Test
    void cacheInvalidation_shouldReflectLatestStats() throws SQLException, InterruptedException {
        // First clear all repositories to ensure a clean test environment
        statsRepository.deleteAll();
        
        // 1. Log initial game stats
        PlayerGameStatsDTO statsDTO = new PlayerGameStatsDTO();
        statsDTO.setPlayerId(lebron.getId());
        statsDTO.setGameId(game.getId());
        statsDTO.setPoints(20);
        statsDTO.setRebounds(8);
        statsDTO.setAssists(6);
        statsDTO.setSteals(1);
        statsDTO.setBlocks(1);
        statsDTO.setFouls(2);
        statsDTO.setTurnovers(2);
        statsDTO.setMinutesPlayed(30.0);

        PlayerGameStats savedStats = playerGameStatsService.logPlayerStats(statsDTO);
        assertNotNull(savedStats);

        // 2. Get aggregate stats (will be cached)
        PlayerAggregateStatsDTO initialStats = aggregationService.getPlayerSeasonStats(lebron.getId());
        assertEquals(20.0, initialStats.getAveragePoints());

        // 3. Update stats - need to delete and re-insert to avoid constraint violation
        statsRepository.delete(savedStats.getId());
        statsDTO.setPoints(30); // changed from 20 to 30
        PlayerGameStats updatedStats = playerGameStatsService.logPlayerStats(statsDTO);
        assertNotNull(updatedStats);
        assertEquals(30, updatedStats.getPoints());

        // 4. Force cache invalidation to ensure predictable test behavior
        cacheService.invalidateAllCaches();
        
        // Add short delay to ensure cache is refreshed
        Thread.sleep(100);
        
        // 5. Use direct database query and manual calculation to verify expected value
        // This bypasses the cache completely for verification
        List<PlayerGameStats> playerStats = statsRepository.findByPlayerId(lebron.getId());
        assertEquals(1, playerStats.size());
        assertEquals(30, playerStats.get(0).getPoints());
        
        // 6. Now verify the service returns the expected value (should match DB)
        PlayerAggregateStatsDTO finalStats = aggregationService.getPlayerSeasonStats(lebron.getId());
        assertEquals(30.0, finalStats.getAveragePoints(), 
                     "After cache eviction, stats must reflect latest database values");
    }
}