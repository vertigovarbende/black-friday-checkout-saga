# kafka-basics

Faz 1 deney uygulaması. Kafka'nın davranışını compose'daki üç node'lu cluster üzerinde görmek için yazıldı; servislerin mimari kurallarına (hexagonal, ArchUnit) uymaz. Saga yolundaki servisler Kafka'ya `KafkaTemplate` ile değil, outbox + Debezium ile yazar (ADR-0005).

```bash
docker compose -f infra/docker/compose.yml --profile core --profile tools up -d
mvn -pl experiments/kafka-basics spring-boot:run
```

Uygulama `localhost:8100`'de açılır. İkinci bir instance için: `PORT=8101 mvn -pl experiments/kafka-basics spring-boot:run`.

| # | Deney |
|---|---|
| 1 | Topic'lerin kodla tanımlanması; olmayan topic'e yazmak; partition sayısını değiştirmek |
| 2 | `acks`, `min.insync.replicas`, broker kapatmak |
| 3 | Key → partition → sıralama; farklı topic'ler arasında sıra; hot partition |
| 4 | Consumer group, rebalance, offset commit, at-least-once; yavaş consumer |
| 5 | Bloklayan retry + DLT; bozuk mesaj (poison pill) |
| 6 | Dual write: DB + `KafkaTemplate` ile doğrudan yol, çökme senaryoları; outbox yoluyla karşılaştırma (Faz 2) |

Deney 6'dan beri uygulama Postgres'teki `experiments` DB'sine bağlanır (tablo `schema.sql` ile açılır). DB, init script'le yeni volume'de kendiliğinden gelir; eski bir volume'de bir kez elle açılır (`docs/setup/local-development.md` §4.1).
