-- Define the initial members table
-- This does very little at this point, but it's the initial proof of concept

-- !Ups

CREATE TABLE members (
  -- The Badge Number of this member, from CM
  badge_number text NOT NULL,
  -- The Badge Name of this member, from CM
  badge_name text NOT NULL,
  -- This becomes true iff this member is removed from the convention
  removed boolean DEFAULT FALSE,
  PRIMARY KEY (badge_number)
);

-- !Downs

DROP TABLE members;
