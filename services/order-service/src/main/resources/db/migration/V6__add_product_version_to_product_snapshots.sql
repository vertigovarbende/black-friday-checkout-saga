-- product.changed'deki productVersion: ürünün product-service'teki sürümü. version kolonu ise bu satırın
-- optimistic lock sürümüdür; ikisi farklı şeyler. Seed satırları 0 ile başlar.
ALTER TABLE product_snapshots ADD COLUMN product_version bigint NOT NULL DEFAULT 0;

ALTER TABLE product_snapshots ADD CONSTRAINT product_snapshots_product_version_ck CHECK (product_version >= 0);
