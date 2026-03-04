DROP TABLE IF EXISTS video CASCADE;
CREATE TABLE video (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    video_name VARCHAR(255) NOT NULL,
    video_description TEXT,
    video_path VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);