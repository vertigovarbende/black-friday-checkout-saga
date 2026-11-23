-- Servislerdeki inbox migration'ıyla birebir aynı (order-service: V5__create_inbox_event.sql, adım 7b).
CREATE TABLE inbox_event
(
    consumer     varchar(100) NOT NULL,
    event_id     uuid         NOT NULL,
    event_type   varchar(128) NOT NULL,
    aggregate_id varchar(128) NOT NULL,
    processed_at timestamptz  NOT NULL,

    CONSTRAINT inbox_event_pk PRIMARY KEY (consumer, event_id)
);

CREATE INDEX inbox_event_processed_at_ix ON inbox_event (processed_at);
