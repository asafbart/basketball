# NBA Player Statistics API

This is a scalable, fault-tolerant system for tracking and analyzing NBA player statistics.

## Features

- Log player statistics per game
- Calculate aggregate statistics per player and team for a season
- Real-time data availability
- High availability and fault tolerance
- Containerized deployment

## Tech Stack

- Java 11
- Spring Boot 2.7
- MySQL 8
- Redis for caching
- Docker & Docker Compose
- Nginx for load balancing

## API Endpoints

### Log Player Statistics

```
POST /api/players/{playerId}/stats      # Log statistics for a specific player
POST /api/games/{gameId}/stats          # Log statistics for a player in a specific game
POST /api/games/{gameId}/stats/batch    # Log statistics for multiple players in a game
```

Each POST request automatically invalidates relevant caches to ensure data consistency.

### Retrieve Player Statistics

```
GET /api/players/{playerId}/stats       # Get all game statistics for a player
GET /api/players/{playerId}/season-stats # Get aggregated season statistics for a player
GET /api/games/{gameId}/stats           # Get all player statistics for a game
```

Season statistics endpoints utilize Redis caching for improved performance.

### Retrieve Team Statistics

```
GET /api/teams/{teamId}/season-stats    # Get aggregated season statistics for a team
GET /api/teams/season-stats             # Get aggregated statistics for all teams
```

Team statistics are cached and automatically refreshed when player statistics are updated.

## Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── nba/
│   │   │           └── stats/
│   │   │               ├── config/
│   │   │               ├── controller/
│   │   │               ├── dto/
│   │   │               ├── exception/
│   │   │               ├── model/
│   │   │               ├── repository/
│   │   │               ├── service/
│   │   │               └── NbaStatsApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── schema.sql
│   └── test/
├── docker-compose.yml
├── Dockerfile
├── nginx.conf
├── pom.xml
└── README.md
```

## Setup & Deployment

### Prerequisites

- Docker and Docker Compose
- Java 11

### Run Locally

1. Clone the repository
2. Run: `docker-compose up --build`
3. Access the API at: `http://localhost:80/nba-stats`

### Running Tests

1. Run unit tests: `./mvnw test`
2. Run integration tests: `./mvnw test -Dtest=StatsRestIntegrationTest`

The integration tests provide a comprehensive end-to-end validation of the API functionality:
- See `src/test/java/com/nba/stats/integration/StatsRestIntegrationTest.java` for complete REST API workflow testing
- This demonstrates creating player statistics, retrieving them, and verifying cache management
- The test verifies that updating player statistics correctly reflects in aggregated data

## Architecture

This application follows a layered architecture:

1. **Controller Layer**: Handles HTTP requests and responses
2. **Service Layer**: Implements business logic
3. **Repository Layer**: Manages data access
4. **Model Layer**: Represents domain entities

## Scalability & High Availability

- Containerized setup with multiple application instances
- Load balancing via Nginx
- Connection pooling for database access
- Redis caching for improved read performance
- Optimized queries with proper indexing

## Thread Safety & Concurrency

- All services use read-write locks to ensure thread safety
- Redis-based caching for high read throughput
- Cache invalidation for real-time data availability

## Cache Management

- Dedicated CacheService with hybrid approach:
  - Uses Spring's @Cacheable and @CacheEvict annotations for simple operations
  - Direct CacheManager access for critical operations like multi-cache invalidation
- Immediate cache invalidation on stats updates to ensure data consistency
- Cache invalidation strategy:
  - Player-specific invalidation when updating individual player stats
  - Team-based invalidation when updating any player on the team
  - Targeted cache eviction based on affected entities

## Performance Optimization

- Redis caching for frequently accessed aggregate statistics
- Optimistic concurrency control
- Connection pooling with HikariCP
- Proper database indexing
- Focused on high read throughput

## Database Schema

- teams: Store team information
- players: Store player information with team relationship
- games: Store game information with team relationships
- player_game_stats: Store player statistics per game
- seasons: Store season information