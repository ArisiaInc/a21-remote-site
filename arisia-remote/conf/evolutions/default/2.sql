-- !Ups

-- The table of permissions for a given member. Note that we intentionally do not bother with this record for
-- every member: this is just for people who have elevated permissions on the Remote site of one sort or
-- another. These are set via the Admin interface.

CREATE TABLE permissions (
  -- The username of this member, from CM
  username text NOT NULL,
  -- Set only for a few people with "super-admin" rights, able to designate other sorts of admins
  -- This bit does not exist in the UI: it can only be set directly in the database
  super_admin boolean DEFAULT FALSE NOT NULL,
  -- Standard admin: this means you can use the Admin pages
  admin boolean DEFAULT FALSE NOT NULL,
  -- True iff this person has early access to the Remote site, for testing and such
  early_access boolean DEFAULT FALSE NOT NULL,
  PRIMARY KEY (username)
);

-- To begin with, Justin is a super-admin -- we should add a few more later, but this list will always be small:

INSERT INTO permissions VALUES ('jducoeur', TRUE, TRUE, TRUE);

-- !Downs

DROP TABLE permissions;
