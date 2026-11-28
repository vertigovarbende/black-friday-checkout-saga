-- Sipariş başına stok rezervasyonu; id = orderId (event-catalog.md §1). Satırsız RELEASED kayıt kapanış kaydıdır
-- (checkout-saga.md §6). Satırların ürünlere FK'si yok: UNKNOWN_PRODUCT ile reddedilen bir rezervasyon bilinmeyen
-- ürünü de satır olarak taşır. requested_at domain'in createdAt'idir (iş zamanı); created_at BaseEntity'nin teknik zamanı.
CREATE TABLE stock_reservations
(
    id                  uuid        PRIMARY KEY,
    status              varchar(20) NOT NULL,
    rejection_reason    varchar(30),
    rejected_product_id uuid,
    requested_at        timestamptz NOT NULL,
    last_modified_at    timestamptz NOT NULL,
    version             bigint      NOT NULL,
    created_at          timestamptz NOT NULL,
    updated_at          timestamptz NOT NULL,

    CONSTRAINT stock_reservations_status_ck CHECK (status IN ('RESERVED', 'COMMITTED', 'RELEASED', 'REJECTED')),
    CONSTRAINT stock_reservations_rejection_reason_ck CHECK (rejection_reason IN ('UNKNOWN_PRODUCT', 'PRICE_CHANGED', 'INSUFFICIENT_STOCK')),
    CONSTRAINT stock_reservations_rejection_ck CHECK ((status = 'REJECTED') = (rejection_reason IS NOT NULL)),
    CONSTRAINT stock_reservations_rejected_product_ck CHECK ((rejection_reason IS NULL) = (rejected_product_id IS NULL))
);

-- PK: aynı ürün bir rezervasyonda iki satırda olamaz.
CREATE TABLE stock_reservation_lines
(
    reservation_id uuid    NOT NULL REFERENCES stock_reservations (id),
    product_id     uuid    NOT NULL,
    quantity       integer NOT NULL,

    CONSTRAINT stock_reservation_lines_pk PRIMARY KEY (reservation_id, product_id),
    CONSTRAINT stock_reservation_lines_quantity_ck CHECK (quantity > 0)
);
