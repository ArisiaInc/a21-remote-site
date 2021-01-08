-- !Ups

CREATE TABLE user_info (
  username text NOT NULL,
  badge_number text NOT NULL,
  membership_type text NOT NULL,
  discord_username text,
  discord_discriminator text,
  discord_id text,
  PRIMARY KEY (username, badge_number)
);

CREATE INDEX discord_index ON user_info (discord_username, discord_discriminator);

-- !Downs

DROP TABLE user_info;
