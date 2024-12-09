-- Create the database
CREATE DATABASE colorgame_db;

-- Switch to the new database
USE colorgame_db;

-- Create the players table
CREATE TABLE players (
    id INT AUTO_INCREMENT PRIMARY KEY,   -- Unique identifier for each player
    name VARCHAR(100),                    -- Name of the player
    email VARCHAR(100) NOT NULL UNIQUE    -- Unique email for player, cannot be null
);

-- Create the game_sessions table
CREATE TABLE game_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,   -- Unique identifier for each game session
    player_id INT NOT NULL,               -- Player's ID (foreign key)
    score INT NOT NULL,                   -- Score of the player in the session
    start_time DATETIME NOT NULL,         -- Start time of the session
    end_time DATETIME NOT NULL,           -- End time of the session
    FOREIGN KEY (player_id) REFERENCES players(id)  -- Foreign key referencing players table
    ON DELETE CASCADE                    -- Optional: cascade delete to remove game_sessions when player is deleted
    ON UPDATE CASCADE                    -- Optional: cascade update if player ID is updated
);

-- Create the color_stats table
CREATE TABLE color_stats (
    color_name VARCHAR(50) PRIMARY KEY,   -- Unique color name
    correct_guesses INT DEFAULT 0,        -- Number of correct guesses for the color, default value of 0
    incorrect_guesses INT DEFAULT 0       -- Number of incorrect guesses for the color, default value of 0
);

select* from players;
select * from game_sessions;
select *from color_stats;