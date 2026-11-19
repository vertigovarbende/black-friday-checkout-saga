CREATE TABLE product_snapshots
(
    id         uuid          PRIMARY KEY,
    name       varchar(200)  NOT NULL,
    unit_price numeric(19,4) NOT NULL,
    currency   varchar(3)    NOT NULL,
    version    bigint        NOT NULL,
    created_at timestamptz   NOT NULL,
    updated_at timestamptz   NOT NULL,

    CONSTRAINT product_snapshots_unit_price_ck CHECK (unit_price > 0)
);
