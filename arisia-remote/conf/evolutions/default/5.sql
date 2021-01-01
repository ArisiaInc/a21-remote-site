-- !Ups

CREATE TABLE zoom_rooms (
  did integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  display_name text NOT NULL,
  zoom_id text NOT NULL,
  zambia_name text DEFAULT '' NOT NULL,
  manual boolean DEFAULT FALSE NOT NULL,
  webinar boolean DEFAULT FALSE NOT NULL
);

CREATE TABLE active_program_items (
  end_at bigint NOT NULL,
  program_item_id text NOT NULL,
  zoom_meeting_id bigint NOT NULL,
  host_url text NOT NULL,
  attendee_url text NOT NULL
);

-- !Downs

DROP TABLE zoom_rooms;

DROP TABLE active_program_items;
