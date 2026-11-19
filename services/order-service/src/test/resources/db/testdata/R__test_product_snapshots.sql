-- Test seed'i (07 §6.3). Değerler OrderValidTestData.Product ve .SecondProduct ile aynıdır; testler bu satırları değiştirmez.
INSERT INTO product_snapshots (id, name, unit_price, currency, version, created_at, updated_at)
VALUES ('0198a1b2-0000-7000-8000-000000000101', 'Wireless Headphones', 249.90, 'TRY', 0, now(), now()),
       ('0198a1b2-0000-7000-8000-000000000102', 'USB-C Cable',          15.00, 'TRY', 0, now(), now())
ON CONFLICT (id) DO NOTHING;
