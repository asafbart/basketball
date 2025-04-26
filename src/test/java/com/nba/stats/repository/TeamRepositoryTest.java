package com.nba.stats.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.nba.stats.model.Team;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@ContextConfiguration(initializers = {TeamRepositoryTest.Initializer.class})
public class TeamRepositoryTest {

    @Container
    public static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("nba_stats_test")
            .withUsername("test")
            .withPassword("test");
    
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + mySQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + mySQLContainer.getUsername(),
                    "spring.datasource.password=" + mySQLContainer.getPassword(),
                    "spring.cache.type=none" // Disable Redis for repository tests
            ).applyTo(context.getEnvironment());
        }
    }

    @Autowired
    private TeamRepository teamRepository;

    @BeforeEach
    void setUp() throws SQLException {
        teamRepository.deleteAll();
    }

    @Test
    void save_shouldSaveTeam() throws SQLException {
        // Arrange
        Team team = new Team(null, "Lakers", "Los Angeles");
        
        // Act
        Team savedTeam = teamRepository.save(team);
        
        // Assert
        assertNotNull(savedTeam);
        assertNotNull(savedTeam.getId());
        assertEquals("Lakers", savedTeam.getName());
        assertEquals("Los Angeles", savedTeam.getCity());
    }
    
    @Test
    void findById_shouldReturnTeam_whenTeamExists() throws SQLException {
        // Arrange
        Team team = new Team(null, "Lakers", "Los Angeles");
        Team savedTeam = teamRepository.save(team);
        
        // Act
        Optional<Team> foundTeam = teamRepository.findById(savedTeam.getId());
        
        // Assert
        assertTrue(foundTeam.isPresent());
        assertEquals("Lakers", foundTeam.get().getName());
    }
    
    @Test
    void findById_shouldReturnEmpty_whenTeamDoesNotExist() throws SQLException {
        // Act
        Optional<Team> foundTeam = teamRepository.findById(999L);
        
        // Assert
        assertFalse(foundTeam.isPresent());
    }
    
    @Test
    void findAll_shouldReturnAllTeams() throws SQLException {
        // Arrange
        Team lakers = new Team(null, "Lakers", "Los Angeles");
        Team celtics = new Team(null, "Celtics", "Boston");
        teamRepository.save(lakers);
        teamRepository.save(celtics);
        
        // Act
        List<Team> teams = teamRepository.findAll();
        
        // Assert
        assertEquals(2, teams.size());
        assertTrue(teams.stream().anyMatch(t -> t.getName().equals("Lakers")));
        assertTrue(teams.stream().anyMatch(t -> t.getName().equals("Celtics")));
    }
    
    @Test
    void update_shouldUpdateTeam() throws SQLException {
        // Arrange
        Team team = new Team(null, "Lakers", "Los Angeles");
        Team savedTeam = teamRepository.save(team);
        
        // Act
        savedTeam.setName("LA Lakers");
        Team updatedTeam = teamRepository.save(savedTeam);
        
        // Assert
        assertEquals("LA Lakers", updatedTeam.getName());
        
        // Verify in database
        Optional<Team> foundTeam = teamRepository.findById(savedTeam.getId());
        assertTrue(foundTeam.isPresent());
        assertEquals("LA Lakers", foundTeam.get().getName());
    }
    
    @Test
    void delete_shouldRemoveTeam() throws SQLException {
        // Arrange
        Team team = new Team(null, "Lakers", "Los Angeles");
        Team savedTeam = teamRepository.save(team);
        
        // Act
        teamRepository.delete(savedTeam.getId());
        
        // Assert
        Optional<Team> foundTeam = teamRepository.findById(savedTeam.getId());
        assertFalse(foundTeam.isPresent());
    }
}