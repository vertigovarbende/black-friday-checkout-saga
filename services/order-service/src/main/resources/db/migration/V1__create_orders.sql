CREATE TABLE orders
(
    id                  uuid        PRIMARY KEY,
    customer_id         uuid        NOT NULL,
    status              varchar(20) NOT NULL,
    cancellation_reason varchar(30),
    placed_at           timestamptz NOT NULL,
    last_modified_at    timestamptz NOT NULL,
    version             bigint      NOT NULL,
    created_at          timestamptz NOT NULL,
    updated_at          timestamptz NOT NULL,

    CONSTRAINT orders_status_ck CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT orders_cancellation_reason_ck CHECK ((status = 'CANCELLED') = (cancellation_reason IS NOT NULL))
);

CREATE INDEX orders_customer_placed_at_ix ON orders (customer_id, placed_at DESC);

CREATE TABLE order_lines
(
    id           uuid          PRIMARY KEY,
    order_id     uuid          NOT NULL REFERENCES orders (id),
    line_no      integer       NOT NULL,
    product_id   uuid          NOT NULL,
    product_name varchar(200)  NOT NULL,
    unit_price   numeric(19,4) NOT NULL,
    currency     varchar(3)    NOT NULL,
    quantity     integer       NOT NULL,

    CONSTRAINT order_lines_unit_price_ck CHECK (unit_price > 0),
    CONSTRAINT order_lines_quantity_ck CHECK (quantity > 0),
    CONSTRAINT order_lines_order_product_uk UNIQUE (order_id, product_id),
    CONSTRAINT order_lines_order_line_no_uk UNIQUE (order_id, line_no)
);
