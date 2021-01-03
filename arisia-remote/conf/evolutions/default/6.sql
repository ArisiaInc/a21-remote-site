-- !Ups

CREATE TABLE ducks (
  did integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  image text NOT NULL,
  alt text NOT NULL,
  link text NOT NULL,
  hint text,
  requesting_url text NOT NULL
);

CREATE TABLE member_ducks (
  username text NOT NULL,
  duck_id integer NOT NULL
);

-- !Downs

DROP TABLE ducks;

DROP TABLE member_ducks;
