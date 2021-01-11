-- !Ups

ALTER TABLE user_info ADD COLUMN badge_name text DEFAULT '' NOT NULL;

-- !Downs

ALTER TABLE user_info DROP COLUMN badge_name;
