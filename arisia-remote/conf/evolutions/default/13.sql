-- !Ups

ALTER TABLE active_program_items ADD COLUMN webinar boolean DEFAULT FALSE NOT NULL;

-- !Downs

ALTER TABLE active_program_items DROP COLUMN webinar;
