-- !Ups

CREATE TABLE user_settings (
  username text NOT NULL,
  k text NOT NULL,
  v text NOT NULL,
  PRIMARY KEY (username, k)
);

-- !Downs

DROP TABLE user_settings;
