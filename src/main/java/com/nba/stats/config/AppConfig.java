package com.nba.stats.config;

import com.nba.stats.repository.GameRepository;
import com.nba.stats.repository.PlayerGameStatsRepository;
import com.nba.stats.repository.PlayerRepository;
import com.nba.stats.repository.SeasonRepository;
import com.nba.stats.repository.TeamRepository;
import com.nba.stats.service.CacheService;
import com.nba.stats.service.PlayerGameStatsService;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

@Configuration
public class AppConfig {
    
    @Value("${spring.datasource.url}")
    private String dbUrl;
    
    @Value("${spring.datasource.username}")
    private String dbUsername;
    
    @Value("${spring.datasource.password}")
    private String dbPassword;
    
    @Value("${spring.datasource.driver-class-name}")
    private String dbDriverClassName;
    
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName(dbDriverClassName);
        
        // Connection pool settings
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(10000);
        
        return new HikariDataSource(config);
    }
    
    @Bean
    public TeamRepository teamRepository(DataSource dataSource) {
        return new TeamRepository(dataSource);
    }
    
    @Bean
    public PlayerRepository playerRepository(DataSource dataSource, TeamRepository teamRepository) {
        return new PlayerRepository(dataSource, teamRepository);
    }
    
    @Bean
    public GameRepository gameRepository(DataSource dataSource, TeamRepository teamRepository) {
        return new GameRepository(dataSource, teamRepository);
    }
    
    @Bean
    public SeasonRepository seasonRepository(DataSource dataSource) {
        return new SeasonRepository(dataSource);
    }
    
    @Bean
    public PlayerGameStatsRepository playerGameStatsRepository(DataSource dataSource, 
                                                             PlayerRepository playerRepository,
                                                             GameRepository gameRepository) {
        return new PlayerGameStatsRepository(dataSource, playerRepository, gameRepository);
    }
    
    @Bean
    public PlayerGameStatsService playerGameStatsService(PlayerGameStatsRepository statsRepository,
                                                       PlayerRepository playerRepository,
                                                       GameRepository gameRepository,
                                                       CacheService cacheService) {
        return new PlayerGameStatsService(statsRepository, playerRepository, gameRepository, cacheService);
    }
    
    // Removed StatsAggregationService bean definition because it's already defined with @Service annotation
}