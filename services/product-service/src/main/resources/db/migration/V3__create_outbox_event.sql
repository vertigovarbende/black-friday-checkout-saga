CREATE TABLE outbox_event
(
    id             uuid         PRIMARY KEY,
    aggregate_type varchar(64)  NOT NULL,
    aggregate_id   varchar(128) NOT NULL,
    event_type     varchar(128) NOT NULL,
    topic          varchar(128) NOT NULL,
    payload        jsonb        NOT NULL,
    traceparent    varchar(64),
    created_at     timestamptz  NOT NULL
);

CREATE INDEX outbox_event_created_at_ix ON outbox_event (created_at);
