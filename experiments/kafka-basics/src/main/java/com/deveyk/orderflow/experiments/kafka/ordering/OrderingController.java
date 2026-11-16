package com.deveyk.orderflow.experiments.kafka.ordering;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/ordering")
public class OrderingController {

    private static final String HOT_KEY = "product-iphone";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AtomicInteger runs = new AtomicInteger();

    public OrderingController(final KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/burst")
    public Map<String, Integer> burst(@RequestParam final List<String> keys,
                                      @RequestParam(defaultValue = "10") final int count) throws Exception {

        final Map<String, Integer> partitionByKey = new TreeMap<>();

        for (int sequence = 1; sequence <= count; sequence++) {
            for (final String key : keys) {
                final SendResult<String, String> result = kafkaTemplate
                        .send(OrderingTopics.CREATED, key, key + "#" + sequence)
                        .get(10, TimeUnit.SECONDS);

                partitionByKey.put(key, result.getRecordMetadata().partition());
            }
        }

        return partitionByKey;
    }

    @PostMapping("/created-then-cancelled")
    public Map<String, Object> createdThenCancelled(@RequestParam(defaultValue = "20") final int count) throws Exception {
        final String run = "r" + runs.incrementAndGet();

        for (int i = 1; i <= count; i++) {
            final String orderId = run + "-o-" + i;
            kafkaTemplate.send(OrderingTopics.CREATED, orderId, "created").get(10, TimeUnit.SECONDS);
            kafkaTemplate.send(OrderingTopics.CANCELLED, orderId, "cancelled").get(10, TimeUnit.SECONDS);
        }

        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("run", run);
        result.put("orders", count);

        return result;
    }

    @PostMapping("/hot")
    public Map<String, Object> hot(@RequestParam(defaultValue = "2000") final int count,
                                   @RequestParam(defaultValue = "0.9") final double hotRatio) throws Exception {

        final List<CompletableFuture<SendResult<String, String>>> sends = new ArrayList<>(count);
        final ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 1; i <= count; i++) {
            final String key = random.nextDouble() < hotRatio ? HOT_KEY : "product-" + random.nextInt(1000);
            sends.add(kafkaTemplate.send(OrderingTopics.HOT, key, key + "#" + i));
        }

        CompletableFuture.allOf(sends.toArray(CompletableFuture[]::new)).get(30, TimeUnit.SECONDS);

        final Map<Integer, Integer> messagesByPartition = new TreeMap<>();
        for (final CompletableFuture<SendResult<String, String>> send : sends) {
            messagesByPartition.merge(send.join().getRecordMetadata().partition(), 1, Integer::sum);
        }

        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("hotKey", HOT_KEY);
        result.put("messagesByPartition", messagesByPartition);

        return result;
    }
}
