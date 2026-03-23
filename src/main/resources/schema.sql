DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password TEXT NOT NULL,

    channel_tag VARCHAR(64) UNIQUE,
    channel_name VARCHAR(255) NOT NULL,
    channel_description VARCHAR(255),

    banner_path TEXT,
    miniature_path TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS video CASCADE;
CREATE TABLE video (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    video_name VARCHAR(255) NOT NULL,
    video_description TEXT,
    thumbnail_path VARCHAR(500) NOT NULL,
    video_path VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    owner_id INTEGER NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);