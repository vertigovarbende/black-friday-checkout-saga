# Black Friday Checkout Saga

Black Friday yükü altında çalışan bir e-ticaret sipariş sistemi üzerinden **event-driven mimariyi** uygulamalı öğrenmek için kurulmuş, gerçekten çalışan bir lab projesi.

Kullanıcı sipariş verir, stok ayrılır, ödeme alınır ve sipariş tamamlanır. Bir adım başarısız olursa önceki adımlar telafi edilir. Aynı anda canlı bir panelde satışlar akar. Bu akışın her parçası, öğrenilecek bir konuyu gerçekten yaşatacak şekilde tasarlandı: Kafka, outbox / inbox, orkestrasyonlu saga, Debezium, Redis, HikariCP ile primary / replica, observability, yük ve chaos testleri.

> **Durum:** Tasarım tamamlandı, geliştirme Faz 0'dan başlıyor. Kararlar `docs/decisions/` ve `docs/adr/` altında; sıra `docs/ROADMAP.md`'de.

---

## Mimari bir bakışta

[`docs/diagrams/blackfriday.excalidraw`](docs/diagrams/blackfriday.excalidraw) (excalidraw.com → Open).

| Servis | Sahip olduğu veri | Görevi |
|---|---|---|
| **order-service** | siparişler, ürünlerin yerel kopyası | Siparişi alır (`202 Accepted`), saga sonunda tamamlar ya da iptal eder |
| **orchestrator-service** | saga durumu ve adım geçmişi | `CheckoutSaga`'yı yürütür, komut gönderir, timeout ve telafiyi yönetir |
| **product-service** | ürünler, stok, rezervasyonlar | Stok ayırır, bırakır, kesinleştirir; flash sale stok kapısı |
| **payment-service** | ödemeler, cüzdanlar | authorize → capture, telafi olarak void |
| **notification-service** | gönderilen bildirimler | Sipariş olaylarına bildirim üretir |
| **feed-service** | veritabanı yok, sadece Redis | Kafka → Redis Streams → Pub/Sub → SSE ile canlı panel |

**Temel kurallar:**
- Servisler arasında REST yok; her şey Kafka üzerinden.
- Üretici tarafında **outbox** (Debezium CDC), tüketici tarafında **inbox**. Inbox kaydı, iş ve cevap outbox'ı tek transaction'da.
- Her servisin kendi veritabanı var; tek bir Postgres cluster'ında (1 primary + 1–2 replica). Komutlar primary'ye, sorgular replica'ya gider.
- Her servis içinde hexagonal mimari: `domain` / `application` / `adapter` / `config`. Sınırları ArchUnit korur.

---

## Teknoloji

| | |
|---|---|
| Dil / framework | Java 25 · Spring Boot 4.x · Maven multi-module |
| Mesajlaşma | Apache Kafka (KRaft, 3 broker) · Spring Kafka · Debezium (Kafka Connect) |
| Veri | PostgreSQL (primary + replica) · HikariCP · Spring Data JPA · Flyway |
| Redis | Streams · Pub/Sub · Lua (rate limit, stok kapısı) · distributed lock |
| Gözlem | Micrometer · OpenTelemetry · Prometheus · Grafana Alloy · Loki · Tempo · Grafana |
| Test | JUnit · AssertJ · Mockito · ArchUnit · Testcontainers · Awaitility · Toxiproxy · k6 |
| Ortam | Docker Compose (`core` · `cluster` · `observability`) → sonra kind + kubectl + k9s |

---

## Repo yapısı

```
orderflow-v2/
├── shared/            ortak teknik modüller (shared-domain, shared-messaging, shared-outbox, …)
├── services/          6 servis
├── e2e-tests/         servisler arası E2E ve chaos testleri
├── infra/             compose, Debezium connector'ları, observability ayarları
├── load-tests/k6/     yük testleri
├── tools/             tutarlılık denetçisi
└── docs/              kararlar, ADR'ler, yol haritası, çizimler
```

Ayrıntı: [`docs/decisions/01-paket-yapisi-kararlari.md`](docs/decisions/01-paket-yapisi-kararlari.md).

---

## Çalıştırma

```bash
# altyapı: Postgres (primary + replica), Kafka (3 node, KRaft), Kafka Connect (Debezium), Redis
docker compose -f infra/docker/compose.yml --profile core up -d

# build ve testler (integration testleri için Docker açık olmalı)
mvn verify
```

Kurulum (WSL bellek sınırı, portlar, kontroller, sık görülen sorunlar): [`docs/setup/local-development.md`](docs/setup/local-development.md).

Hedef makine: 4 çekirdek, 16 GB RAM, Windows + WSL2. Compose profilleri bu sınıra göre ayrıldı.

---
