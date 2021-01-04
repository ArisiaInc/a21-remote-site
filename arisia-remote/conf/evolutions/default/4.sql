-- !Ups

-- This is a simple join table (conceptually) of users and their starred program items.

CREATE TABLE starred_items (
  login_id text NOT NULL,
  item_id text NOT NULL
);

-- Note that we specifically want a non-unique index, so it's not a primary key:

CREATE INDEX star_index ON starred_items (login_id);

-- !Downs

DROP TABLE starred_items;
