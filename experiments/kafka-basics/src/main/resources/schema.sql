-- Deney 6 (dual write): doğrudan yolun sipariş tablosu. run: aynı topic'teki ayrı deney turlarını ayırır.
CREATE TABLE IF NOT EXISTS dualwrite_order
(
    id         uuid PRIMARY KEY,
    run        varchar(64) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);
