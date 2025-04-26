-- Create tables

-- Teams
CREATE TABLE IF NOT EXISTS teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Players
CREATE TABLE IF NOT EXISTS players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    position VARCHAR(50),
    team_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams(id)
);

-- Seasons
CREATE TABLE IF NOT EXISTS seasons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    year INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (year)
);

-- Games
CREATE TABLE IF NOT EXISTS games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    home_team_id BIGINT NOT NULL,
    away_team_id BIGINT NOT NULL,
    home_score INT NOT NULL DEFAULT 0,
    away_score INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (home_team_id) REFERENCES teams(id),
    FOREIGN KEY (away_team_id) REFERENCES teams(id)
);

-- Player Game Statistics
CREATE TABLE IF NOT EXISTS player_game_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    points INT NOT NULL DEFAULT 0,
    rebounds INT NOT NULL DEFAULT 0,
    assists INT NOT NULL DEFAULT 0,
    steals INT NOT NULL DEFAULT 0,
    blocks INT NOT NULL DEFAULT 0,
    fouls INT NOT NULL DEFAULT 0,
    turnovers INT NOT NULL DEFAULT 0,
    minutes_played DECIMAL(4,1) NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id),
    FOREIGN KEY (game_id) REFERENCES games(id),
    UNIQUE (player_id, game_id)
);

-- Create indexes for performance
CREATE INDEX idx_players_team_id ON players(team_id);
CREATE INDEX idx_games_date ON games(date);
CREATE INDEX idx_games_home_team_id ON games(home_team_id);
CREATE INDEX idx_games_away_team_id ON games(away_team_id);
CREATE INDEX idx_player_game_stats_player_id ON player_game_stats(player_id);
CREATE INDEX idx_player_game_stats_game_id ON player_game_stats(game_id);

-- Add sample data if needed for development/testing
INSERT INTO teams (name, city) VALUES
('Lakers', 'Los Angeles'),
('Warriors', 'Golden State'),
('Celtics', 'Boston'),
('Heat', 'Miami')
ON DUPLICATE KEY UPDATE name = VALUES(name), city = VALUES(city);

-- Insert current season
INSERT INTO seasons (year, start_date, end_date) VALUES
(2025, '2024-10-22', '2025-06-19')
ON DUPLICATE KEY UPDATE start_date = VALUES(start_date), end_date = VALUES(end_date);