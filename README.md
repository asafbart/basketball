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
POST /api/players/{playerId}/stats
POST /api/games/{gameId}/stats
POST /api/games/{gameId}/stats/batch
```

### Retrieve Player Statistics

```
GET /api/players/{playerId}/stats
GET /api/players/{playerId}/season-stats
GET /api/games/{gameId}/stats
```

### Retrieve Team Statistics

```
GET /api/teams/{teamId}/season-stats
GET /api/teams/season-stats
```

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

### Run Locally

1. Clone the repository
2. Run: `docker-compose up --build`
3. Access the API at: `http://localhost:80/nba-stats`

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