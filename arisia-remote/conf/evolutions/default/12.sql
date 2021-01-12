-- !Ups

CREATE TABLE ribbons (
    ribbonid integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    ribbon_text text NOT NULL,
    color_fg text,
    color_bg text,
    gradient text,
    image_fg text,
    image_bg text,
    secret text NOT NULL,
    self_service boolean DEFAULT FALSE NOT NULL
);

CREATE TABLE member_ribbons (
    username text NOT NULL,
    ribbonid text NOT NULL,
    display_order integer NOT NULL
);

CREATE INDEX member_ribbon_index ON member_ribbons (username, ribbonid);

-- !Downs

DROP TABLE ribbons;

DROP TABLE member_ribbons;
