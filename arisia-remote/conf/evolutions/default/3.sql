-- !Ups

-- The format we expect from Zambia has changed from JSONP to JSON, so adjust accordingly
-- Note that this is a new scheduleJson row, which supercedes the old scheduleJsonp one

INSERT INTO text_files VALUES ('scheduleJson', '{"program":[], "people": []}');

-- !Downs
