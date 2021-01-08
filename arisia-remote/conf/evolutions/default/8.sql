-- !Ups

ALTER TABLE zoom_rooms ADD COLUMN discord_name text DEFAULT '' NOT NULL;

-- !Downs

ALTER TABLE zoom_rooms DROP COLUMN discord_name;
