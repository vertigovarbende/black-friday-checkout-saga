package com.deveyk.orderflow.experiments.kafka.group;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/group")
public class GroupController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ProcessedLedger ledger;
    private final SlowListener slowListener;
    private final String instance;

    public GroupController(final KafkaTemplate<String, String> kafkaTemplate,
                           final ProcessedLedger ledger,
                           final SlowListener slowListener,
                           @Value("${server.port}") final String instance) {
        this.kafkaTemplate = kafkaTemplate;
        this.ledger = ledger;
        this.slowListener = slowListener;
        this.instance = instance;
    }

    @PostMapping("/send")
    public Map<String, Object> send(@RequestParam(defaultValue = "60") final int count) throws Exception {
        final List<CompletableFuture<?>> sends = new ArrayList<>(count);

        for (int i = 1; i <= count; i++) {
            sends.add(kafkaTemplate.send(GroupTopics.GROUP, "k-" + i, "m-" + i));
        }

        CompletableFuture.allOf(sends.toArray(CompletableFuture[]::new)).get(30, TimeUnit.SECONDS);

        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("sent", count);
        result.put("values", "m-1..m-" + count);

        return result;
    }

    @GetMapping("/ledger")
    public Map<String, Object> ledger(@RequestParam(defaultValue = "60") final int count) {
        final Map<String, List<String>> instancesByValue = new TreeMap<>();

        for (final String line : ledger.lines()) {
            final String[] parts = line.split(" ");
            instancesByValue.computeIfAbsent(parts[0], value -> new ArrayList<>()).add(parts[1]);
        }

        final Map<String, List<String>> duplicates = new TreeMap<>();
        instancesByValue.forEach((value, instances) -> {
            if (instances.size() > 1) {
                duplicates.put(value, instances);
            }
        });

        final List<String> missing = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            if (!instancesByValue.containsKey("m-" + i)) {
                missing.add("m-" + i);
            }
        }

        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("file", ledger.file().toString());
        result.put("processed", instancesByValue.values().stream().mapToInt(List::size).sum());
        result.put("unique", instancesByValue.size());
        result.put("missing", missing);
        result.put("duplicates", duplicates);

        return result;
    }

    @DeleteMapping("/ledger")
    public Map<String, Object> clearLedger() {
        ledger.clear();
        return Map.of("cleared", ledger.file().toString());
    }

    @PostMapping("/crash")
    public Map<String, Object> crash() {
        final Thread halt = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Runtime.getRuntime().halt(1);
        });
        halt.start();

        return Map.of("halting", instance);
    }

    @PostMapping("/slow")
    public Map<String, Object> slow(@RequestParam(defaultValue = "15000") final long processMs) throws Exception {
        kafkaTemplate.send(GroupTopics.SLOW, "slow", Long.toString(processMs)).get(10, TimeUnit.SECONDS);
        return Map.of("sent", GroupTopics.SLOW, "processMs", processMs);
    }

    @PostMapping("/slow/fix")
    public Map<String, Object> fixSlow() {
        slowListener.fix();
        return Map.of("fixed", instance);
    }
}
