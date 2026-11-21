-- Connector: publication.autocreate.mode = disabled, publication.name = order_outbox_pub.
CREATE PUBLICATION order_outbox_pub FOR TABLE outbox_event;

DO
$$
    BEGIN
        IF EXISTS (SELECT FROM pg_roles WHERE rolname = 'debezium') THEN
            GRANT SELECT ON outbox_event TO debezium;
        END IF;
    END
$$;
