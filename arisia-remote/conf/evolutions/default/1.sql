-- Define the initial tables
-- Once the system begins to stabilize, we shouldn't change this file; instead, add 2.sql for modifications

-- !Ups

-- The table of permissions for a given member. Note that we intentionally do not bother with this record for
-- every member: this is just for people who have elevated permissions on the Remote site of one sort or
-- another.

CREATE TABLE permissions (
  -- The Badge Number of this member, from CM
  badge_number text NOT NULL,
  -- True iff this member has is a Host for panels
  host boolean DEFAULT FALSE,
  -- True iff this is a member of Tech staff for Zoom
  tech boolean DEFAULT FALSE,
  -- True iff this is a member of Safety, capable of removing people from Zoom meetings
  safety boolean DEFAULT FALSE,
  -- True iff this person has early access to the Remote site, for testing and such
  early_access boolean DEFAULT FALSE,
  -- This becomes true iff this member is removed from the convention
  removed boolean DEFAULT FALSE,
  PRIMARY KEY (badge_number)
);

-- The table of our Zoom licenses, and how they correspond to Zambia rooms.

CREATE TABLE zoom_rooms (
  -- The Zoom user ID of this license
  zoom_id text NOT NULL,
  -- The Zambia ID of this room (really an Int, but we'll treat it as a String)
  zambia_id text NOT NULL,
  -- How we display this room in Zambia and publicly
  display_name text NOT NULL,
  PRIMARY KEY (zambia_id)
);

-- The table of Miscellaneous Text Files: big singletons that we aren't normalizing. We are keeping these in the
-- DB instead of on disk because it is easier to work with, and more durable if something goes wrong.

CREATE TABLE text_files (
  name text NOT NULL,
  value text NOT NULL,
  PRIMARY KEY (name)
);

-- !Downs

DROP TABLE permissions;
DROP TABLE zoom_rooms;
DROP TABLE text_files;
