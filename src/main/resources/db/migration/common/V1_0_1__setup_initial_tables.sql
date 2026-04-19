CREATE TABLE IF NOT EXISTS users(
    id TEXT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS user_fitness_metadata(
    user_id varchar(255) NOT NULL,
    steps INT NOT NULL DEFAULT 0,
    heart_beat INT NOT NULL DEFAULT 0,
    met INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) PARTITION BY RANGE (created_at);


CREATE INDEX index_user_fitness_metadata_user_id ON user_fitness_metadata(user_id, created_at);


