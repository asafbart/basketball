package com.nba.stats.integration;

import com.nba.stats.dto.PlayerAggregateStatsDTO;
import com.nba.stats.dto.PlayerGameStatsDTO;
import com.nba.stats.model.Game;
import com.nba.stats.model.Player;
import com.nba.stats.model.PlayerGameStats;
import com.nba.stats.integration.TestPlayerGameStats;
import com.nba.stats.model.Season;
import com.nba.stats.model.Team;
import com.nba.stats.repository.GameRepository;
import com.nba.stats.repository.PlayerGameStatsRepository;
import com.nba.stats.repository.PlayerRepository;
import com.nba.stats.repository.SeasonRepository;
import com.nba.stats.repository.TeamRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.cache.CacheManager;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@ContextConfiguration(initializers = {StatsRestIntegrationTest.Initializer.class})
public class StatsRestIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

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
            // Start containers before accessing mapped ports
            mySQLContainer.start();
            redisContainer.start();
            
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
    private RedisTemplate<String, Object> redisTemplate;


    private Team lakers;
    private Team celtics;
    private Player lebron;
    private Game game;
    private Season currentSeason;
    private String baseUrl;

    private ObjectMapper objectMapper = new ObjectMapper();
    
    // Custom error handler that will print the error response content
    class CustomErrorHandler implements ResponseErrorHandler {
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return new DefaultResponseErrorHandler().hasError(response);
        }
        
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            System.err.println("ERROR STATUS: " + response.getStatusCode());
            System.err.println("ERROR HEADERS: " + response.getHeaders());
            System.err.println("ERROR BODY: " + new String(response.getBody().readAllBytes()));
            
            // Don't throw an exception, just log the error
        }
    }
    
    // Logging interceptor that will log request and response
    class LoggingInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(
                HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            
            // Log request
            System.out.println("REQUEST: " + request.getMethod() + " " + request.getURI());
            System.out.println("HEADERS: " + request.getHeaders());
            System.out.println("BODY: " + new String(body, "UTF-8"));
            
            // Execute and log response
            ClientHttpResponse response = execution.execute(request, body);
            
            System.out.println("RESPONSE STATUS: " + response.getStatusCode());
            System.out.println("RESPONSE HEADERS: " + response.getHeaders());
            
            // We can't read the body here as it will be consumed, but our error handler will log it if needed
            
            return response;
        }
    }
    
    // We're using the LoggingInterceptor for request/response logging
    @BeforeEach
    void setUp() throws SQLException {
        baseUrl = "http://localhost:" + port + "/nba-stats/api";
        System.out.println("Using base URL: " + baseUrl);
        
        // Configure RestTemplate to use our custom error handler and interceptor
        restTemplate.getRestTemplate().setErrorHandler(new CustomErrorHandler());
        restTemplate.getRestTemplate().setInterceptors(
            java.util.Collections.singletonList(new LoggingInterceptor()));
        
        // Clear all repositories
        statsRepository.deleteAll();
        gameRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();
        seasonRepository.deleteAll();
        
        // Debug log repositories
        System.out.println("* Starting test with empty repositories *");

        // Create test data
        lakers = new Team(null, "Lakers", "Los Angeles");
        lakers = teamRepository.save(lakers);
        System.out.println("Saved Lakers team with ID: " + lakers.getId());

        celtics = new Team(null, "Celtics", "Boston");
        celtics = teamRepository.save(celtics);
        System.out.println("Saved Celtics team with ID: " + celtics.getId());

        lebron = new Player(null, "LeBron", "James", "F", lakers);
        lebron = playerRepository.save(lebron);
        System.out.println("Saved LeBron player with ID: " + lebron.getId());

        currentSeason = new Season(null, 2025, LocalDate.of(2024, 10, 22), LocalDate.of(2025, 6, 19));
        currentSeason = seasonRepository.save(currentSeason);

        game = new Game(null, LocalDate.of(2025, 1, 15), lakers, celtics, 120, 115);
        game = gameRepository.save(game);
        System.out.println("Saved Game with ID: " + game.getId());
    }

    @Test
    void fullStatsWorkflow_shouldCreateAndRetrieveStats() throws InterruptedException, SQLException {
        // 1. Create player game stats via REST API
        PlayerGameStatsDTO statsDTO = new PlayerGameStatsDTO();
        statsDTO.setPlayerId(lebron.getId());  // Set the player ID directly in the DTO
        statsDTO.setGameId(game.getId());
        statsDTO.setPoints(30);
        statsDTO.setRebounds(10);
        statsDTO.setAssists(8);
        statsDTO.setSteals(2);
        statsDTO.setBlocks(1);
        statsDTO.setFouls(2);
        statsDTO.setTurnovers(3);
        statsDTO.setMinutesPlayed(35.5);

        String postUrl = baseUrl + "/players/" + lebron.getId() + "/stats";
        
        // Set up headers to ensure JSON content type
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        
        HttpEntity<PlayerGameStatsDTO> request = new HttpEntity<>(statsDTO, headers);
        
        ResponseEntity<Map> postResponse = restTemplate.exchange(
                postUrl, HttpMethod.POST, request, Map.class);
        
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        assertNotNull(postResponse.getBody());
        
        // Access map values rather than object properties
        Integer points = (Integer) postResponse.getBody().get("points");
        assertEquals(30, points);

        // 2. Get aggregate stats via REST API
        String getStatsUrl = baseUrl + "/players/" + lebron.getId() + "/season-stats";
        
        HttpHeaders getHeaders = new HttpHeaders();
        getHeaders.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<?> getRequest = new HttpEntity<>(getHeaders);
        
        ResponseEntity<PlayerAggregateStatsDTO> getStatsResponse = restTemplate.exchange(
                getStatsUrl, HttpMethod.GET, getRequest, PlayerAggregateStatsDTO.class);
        
        assertEquals(HttpStatus.OK, getStatsResponse.getStatusCode());
        PlayerAggregateStatsDTO aggregateStats = getStatsResponse.getBody();
        assertNotNull(aggregateStats);
        assertEquals("LeBron", aggregateStats.getFirstName());
        assertEquals("James", aggregateStats.getLastName());
        assertEquals(1, aggregateStats.getGamesPlayed());
        assertEquals(30.0, aggregateStats.getAveragePoints());
        assertEquals(10.0, aggregateStats.getAverageRebounds());

        // 3. Log another game stats and see if aggregates update
        Game game2 = new Game(null, LocalDate.of(2025, 2, 10), celtics, lakers, 105, 110);
        game2 = gameRepository.save(game2);

        PlayerGameStatsDTO statsDTO2 = new PlayerGameStatsDTO();
        statsDTO2.setPlayerId(lebron.getId());  // Set the player ID directly in the DTO
        statsDTO2.setGameId(game2.getId());
        statsDTO2.setPoints(24);
        statsDTO2.setRebounds(6);
        statsDTO2.setAssists(10);
        statsDTO2.setSteals(1);
        statsDTO2.setBlocks(0);
        statsDTO2.setFouls(3);
        statsDTO2.setTurnovers(2);
        statsDTO2.setMinutesPlayed(32.0);

        HttpEntity<PlayerGameStatsDTO> request2 = new HttpEntity<>(statsDTO2, headers);
        
        ResponseEntity<Map> postResponse2 = restTemplate.exchange(
                postUrl, HttpMethod.POST, request2, Map.class);
        
        assertEquals(HttpStatus.CREATED, postResponse2.getStatusCode());
        assertNotNull(postResponse2.getBody());
        
        // Print the response body for debugging
        System.out.println("Second stats creation response: " + postResponse2.getBody());
        
        // Let's directly check the database to verify both stats are there
        List<PlayerGameStats> playerGameStats = statsRepository.findByPlayerId(lebron.getId());
        System.out.println("Number of player game stats in DB: " + playerGameStats.size());
        for (PlayerGameStats stat : playerGameStats) {
            System.out.println("Stats record - Player: " + stat.getPlayer().getId() + 
                               ", Game: " + stat.getGame().getId() + 
                               ", Points: " + stat.getPoints());
        }
        
        // Try to explicitly clear the cache both through Spring's CacheManager and Redis directly
        System.out.println("Explicitly clearing all caches...");

        
        // Also clear directly through Redis for good measure
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        
        // Cache eviction may take a moment to propagate
        // Increase wait time to ensure cache is refreshed
        Thread.sleep(500);
        
        // 4. Get updated aggregate stats
        // Get current season for query
        Season season = seasonRepository.findCurrentSeason().orElse(null);
        System.out.println("Current season: " + (season != null ? season.getId() : "None"));
        
        // Direct DB query to validate the game count
        if (season != null) {
            List<PlayerGameStats> statsInSeason = statsRepository.findByPlayerIdAndDateRange(
                lebron.getId(), season.getStartDate(), season.getEndDate());
            System.out.println("Stats count in current season by DB query: " + statsInSeason.size());
            for (PlayerGameStats stat : statsInSeason) {
                System.out.println("Season stat - Game: " + stat.getGame().getId() + 
                                  ", Date: " + stat.getGame().getDate() +
                                  ", Points: " + stat.getPoints());
            }
        }
        
        // Now try to get the updated stats via REST API
        ResponseEntity<PlayerAggregateStatsDTO> updatedStatsResponse = restTemplate.exchange(
                getStatsUrl, HttpMethod.GET, getRequest, PlayerAggregateStatsDTO.class);
        
        assertEquals(HttpStatus.OK, updatedStatsResponse.getStatusCode());
        PlayerAggregateStatsDTO updatedStats = updatedStatsResponse.getBody();
        assertNotNull(updatedStats);
        
        // Print the full response
        System.out.println("Updated stats from API: Games played = " + updatedStats.getGamesPlayed());
        System.out.println("Average points: " + updatedStats.getAveragePoints());
        System.out.println("Average rebounds: " + updatedStats.getAverageRebounds());
        System.out.println("Average assists: " + updatedStats.getAverageAssists());
        
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
    void cacheInvalidation_shouldReflectLatestStats() throws InterruptedException, SQLException {
        // 1. Log initial game stats via REST API
        PlayerGameStatsDTO statsDTO = new PlayerGameStatsDTO();
        statsDTO.setPlayerId(lebron.getId());  // Set the player ID directly in the DTO
        statsDTO.setGameId(game.getId());
        statsDTO.setPoints(20);
        statsDTO.setRebounds(8);
        statsDTO.setAssists(6);
        statsDTO.setSteals(1);
        statsDTO.setBlocks(1);
        statsDTO.setFouls(2);
        statsDTO.setTurnovers(2);
        statsDTO.setMinutesPlayed(30.0);

        String postUrl = baseUrl + "/players/" + lebron.getId() + "/stats";
        
        // Set up headers to ensure JSON content type
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        
        HttpEntity<PlayerGameStatsDTO> request = new HttpEntity<>(statsDTO, headers);
        
        ResponseEntity<Map> postResponse = restTemplate.exchange(
                postUrl, HttpMethod.POST, request, Map.class);
        
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        assertNotNull(postResponse.getBody());
        
        // Extract id from the map
        Number idNumber = (Number) postResponse.getBody().get("id");
        Long statsId = idNumber != null ? idNumber.longValue() : null;

        // 2. Get aggregate stats via REST API (will be cached)
        String getStatsUrl = baseUrl + "/players/" + lebron.getId() + "/season-stats";
        
        HttpHeaders getHeaders = new HttpHeaders();
        getHeaders.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<?> getRequest = new HttpEntity<>(getHeaders);
        
        ResponseEntity<PlayerAggregateStatsDTO> getStatsResponse = restTemplate.exchange(
                getStatsUrl, HttpMethod.GET, getRequest, PlayerAggregateStatsDTO.class);
        
        assertEquals(HttpStatus.OK, getStatsResponse.getStatusCode());
        PlayerAggregateStatsDTO initialStats = getStatsResponse.getBody();
        assertNotNull(initialStats);
        assertEquals(20.0, initialStats.getAveragePoints());

        // 3. Update stats by deleting old ones and creating new ones
        // We need to delete and re-create to avoid constraint violations
        statsRepository.delete(statsId);
        
        // Update points in our DTO
        statsDTO.setPoints(30); // changed from 20 to 30
        
        // Post updated stats with new headers
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        
        HttpEntity<PlayerGameStatsDTO> updateRequest = new HttpEntity<>(statsDTO, updateHeaders);
        
        ResponseEntity<Map> updateResponse = restTemplate.exchange(
                postUrl, HttpMethod.POST, updateRequest, Map.class);
        
        assertEquals(HttpStatus.CREATED, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        
        // Access map values rather than object properties
        Integer points = (Integer) updateResponse.getBody().get("points");
        assertEquals(30, points);
        
        // Wait for cache invalidation to take effect
        Thread.sleep(100);
        
        // 4. Get updated aggregate stats
        ResponseEntity<PlayerAggregateStatsDTO> finalResponse = restTemplate.exchange(
                getStatsUrl, HttpMethod.GET, getRequest, PlayerAggregateStatsDTO.class);
        
        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
        PlayerAggregateStatsDTO finalStats = finalResponse.getBody();
        assertNotNull(finalStats);
        
        // Verify the updated value is reflected
        assertEquals(30.0, finalStats.getAveragePoints(), 
                    "After updating stats, the average points should be 30.0");
    }
}