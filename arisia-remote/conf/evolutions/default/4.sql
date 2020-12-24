-- !Ups

-- This is a simple join table (conceptually) of users and their starred program items.

CREATE TABLE starred_items (
  login_id text NOT NULL,
  item_id text NOT NULL,
  PRIMARY KEY (login_id)
);

-- !Downs

DROP TABLE starred_items;
