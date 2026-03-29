CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE talks (
  id UUID PRIMARY KEY,
  speaker_id BIGINT NOT NULL,
  topic VARCHAR(255) NOT NULL,
  talk_date TIMESTAMP NOT NULL,
  conference_type VARCHAR(50) NOT NULL,
  format VARCHAR(50) NOT NULL,
  activity_name VARCHAR(255) NOT NULL,
  activity_date TIMESTAMP NOT NULL,
  created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_talks_users_speaker_id FOREIGN KEY (speaker_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_talks_speaker_id ON talks (speaker_id);

CREATE TABLE lectures (
  id UUID PRIMARY KEY,
  title VARCHAR(500) NOT NULL,
  talk_id UUID NOT NULL,
  speaker_id BIGINT NOT NULL,
  media_s3_key VARCHAR(1024),
  created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_lectures_talks_talk_id FOREIGN KEY (talk_id) REFERENCES talks(id) ON DELETE CASCADE,
  CONSTRAINT fk_lectures_users_speaker_id FOREIGN KEY (speaker_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_lectures_talk_id ON lectures (talk_id);
CREATE INDEX idx_lectures_speaker_id ON lectures (speaker_id);
