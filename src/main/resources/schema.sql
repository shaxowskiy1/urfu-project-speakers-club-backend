CREATE TYPE user_role AS ENUM ('DEVELOPER', 'ANALYST');
CREATE TYPE conference_type AS ENUM ('INTERNAL', 'EXTERNAL');
CREATE TYPE talk_format AS ENUM ('TALK', 'WORKSHOP');

CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  roles user_role[] NOT NULL DEFAULT '{}'::user_role[],
  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE talks (
  id UUID PRIMARY KEY,
  speaker_id BIGINT NOT NULL,
  topic VARCHAR(255) NOT NULL,
  talk_date TIMESTAMP NOT NULL,
  conference_type conference_type NOT NULL,
  format talk_format NOT NULL,
  activity_name VARCHAR(255) NOT NULL,
  activity_date TIMESTAMP NOT NULL,
  created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_talks_users_speaker_id FOREIGN KEY (speaker_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_talks_speaker_id ON talks (speaker_id);
