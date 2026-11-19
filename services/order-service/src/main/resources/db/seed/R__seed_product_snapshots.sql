-- Lokal çalıştırma için ürün kopyaları. product-service'in seed'i (Faz 3) aynı id'leri kullanmalı.
-- Testler bu dosyayı değil, src/test/resources/db/testdata'yı kullanır.
-- Adım 7'den sonra kopyayı product.changed günceller; mevcut satırlara dokunulmasın diye DO NOTHING.
INSERT INTO product_snapshots (id, name, unit_price, currency, version, created_at, updated_at)
VALUES ('0198a1b2-0000-7000-8000-000000001001', 'Wireless Headphones',   249.90, 'TRY', 0, now(), now()),
       ('0198a1b2-0000-7000-8000-000000001002', '4K Smart TV 55"',     18999.00, 'TRY', 0, now(), now()),
       ('0198a1b2-0000-7000-8000-000000001003', 'Mechanical Keyboard',  1299.50, 'TRY', 0, now(), now()),
       ('0198a1b2-0000-7000-8000-000000001004', 'USB-C Cable',            89.90, 'TRY', 0, now(), now()),
       ('0198a1b2-0000-7000-8000-000000001005', 'Game Console',        21499.00, 'TRY', 0, now(), now())
ON CONFLICT (id) DO NOTHING;
