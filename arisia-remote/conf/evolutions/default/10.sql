-- !Ups

-- Make Gail a superadmin

INSERT INTO permissions  (username, super_admin) VALUES ('gailbear', TRUE)
ON CONFLICT (username)
DO UPDATE SET super_admin = TRUE;

-- !Downs

