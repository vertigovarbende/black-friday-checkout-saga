-- Lokal çalıştırma için ürünler. id'ler ve fiyatlar order-service'in seed'iyle (R__seed_product_snapshots.sql)
-- birebir aynı olmalı: farklı fiyat her siparişi PRICE_CHANGED ile reddettirir. Game Console az stoklu (flash sale).
-- Testler bu dosyayı değil, ProductDatabaseCleaner'ın eklediği ürünleri kullanır. Satırlar sonra değişebildiği için
-- (rezervasyon, PATCH) DO NOTHING.
INSERT INTO products (id, name, unit_price, currency, on_hand, reserved, catalog_version, version, created_at, updated_at)
VALUES ('0198a1b2-0000-7000-8000-000000001001', 'Wireless Headphones',   249.90, 'TRY', 100, 0, 1, 0, now(), now()),
       ('0198a1b2-0000-7000-8000-000000001002', '4K Smart TV 55"',     18999.00, 'TRY', 100, 0, 1, 0, now(), now()),
       ('0198a1b2-0000-7000-8000-000000001003', 'Mechanical Keyboard',  1299.50, 'TRY', 100, 0, 1, 0, now(), now()),
       ('0198a1b2-0000-7000-8000-000000001004', 'USB-C Cable',            89.90, 'TRY', 100, 0, 1, 0, now(), now()),
       ('0198a1b2-0000-7000-8000-000000001005', 'Game Console',        21499.00, 'TRY',   5, 0, 1, 0, now(), now())
ON CONFLICT (id) DO NOTHING;
