package com.deveyk.orderflow.experiments.kafka.group;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Component
public class ProcessedLedger {

    private final Path file = Path.of(System.getProperty("java.io.tmpdir"), "orderflow-exp-group-ledger.txt");

    public synchronized void append(final String value, final String instance) {
        try {
            Files.writeString(file, value + " " + instance + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public synchronized List<String> lines() {
        try {
            return Files.exists(file) ? Files.readAllLines(file) : List.of();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public synchronized void clear() {
        try {
            Files.deleteIfExists(file);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path file() {
        return file;
    }
}
