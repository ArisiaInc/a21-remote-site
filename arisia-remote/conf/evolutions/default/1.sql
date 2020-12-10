-- Define the initial tables
-- Once the system begins to stabilize, we shouldn't change this file; instead, add 2.sql for modifications

-- !Ups

-- The table of Miscellaneous Text Files: big singletons that we aren't normalizing. We are keeping these in the
-- DB instead of on disk because it is easier to work with, and more durable if something goes wrong.

CREATE TABLE text_files (
  name text NOT NULL,
  value text NOT NULL,
  PRIMARY KEY (name)
);

-- Start with an empty but valid schedule, so we can count on that being around:
INSERT INTO text_files VALUES ('schedule', 'var program = []; var people = []');

-- !Downs

DROP TABLE text_files;
