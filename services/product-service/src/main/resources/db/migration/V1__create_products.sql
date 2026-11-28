-- Ürün ve stoğu (05 §8). CHECK'ler domain'in değişmezlerinin ikinci güvenlik ağı: bir hata domain'i atlarsa satır
-- yazılmaz; Kafka tarafında bu DataIntegrityViolationException → retry → DLT olur, sessizce kaybolmaz.
CREATE TABLE products
(
    id              uuid          PRIMARY KEY,
    name            varchar(200)  NOT NULL,
    unit_price      numeric(19,4) NOT NULL,
    currency        varchar(3)    NOT NULL,
    on_hand         integer       NOT NULL,
    reserved        integer       NOT NULL,
    catalog_version bigint        NOT NULL,
    version         bigint        NOT NULL,
    created_at      timestamptz   NOT NULL,
    updated_at      timestamptz   NOT NULL,

    CONSTRAINT products_unit_price_ck CHECK (unit_price > 0),
    CONSTRAINT products_stock_ck CHECK (reserved >= 0 AND reserved <= on_hand),
    CONSTRAINT products_catalog_version_ck CHECK (catalog_version >= 1)
);
