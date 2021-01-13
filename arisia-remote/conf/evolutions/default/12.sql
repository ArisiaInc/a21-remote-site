-- !Ups

CREATE INDEX discord_id_index ON user_info (discord_id);

-- !Downs

DROP INDEX discord_id_index;
