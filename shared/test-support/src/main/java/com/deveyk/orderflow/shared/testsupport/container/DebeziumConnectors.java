package com.deveyk.orderflow.shared.testsupport.container;

import org.awaitility.Awaitility;
import org.testcontainers.containers.GenericContainer;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DebeziumConnectors {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};
    private static final Duration RUNNING_TIMEOUT = Duration.ofSeconds(60);

    private DebeziumConnectors() {
    }

    public static void register(final GenericContainer<?> connect, final Path configFile, final Map<String, String> overrides) {

        final String name = configFile.getFileName().toString().replaceFirst("\\.json$", "");
        final Map<String, Object> config = new LinkedHashMap<>(JSON.readValue(configFile.toFile(), MAP));

        config.putAll(overrides);

        final HttpResponse<String> response = send(HttpRequest.newBuilder(uri(connect, "/connectors/" + name + "/config"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(config)))
                .build());

        if (response.statusCode() >= 300) {
            throw new IllegalStateException("Connector " + name + " could not be registered: " + response.statusCode() + " " + response.body());
        }

        Awaitility.await("connector " + name + " running")
                .atMost(RUNNING_TIMEOUT)
                .pollInterval(Duration.ofMillis(500))
                .until(() -> "RUNNING".equals(taskState(connect, name)));
    }

    @SuppressWarnings("unchecked")
    private static String taskState(final GenericContainer<?> connect, final String name) {

        final HttpResponse<String> response = send(HttpRequest.newBuilder(uri(connect, "/connectors/" + name + "/status")).GET().build());
        if (response.statusCode() != 200) {
            return null;
        }

        final List<Map<String, Object>> tasks = (List<Map<String, Object>>) JSON.readValue(response.body(), MAP).get("tasks");
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }

        final Map<String, Object> task = tasks.getFirst();
        if ("FAILED".equals(task.get("state"))) {
            throw new IllegalStateException("Connector " + name + " task failed: " + task.get("trace"));
        }

        return (String) task.get("state");
    }

    private static URI uri(final GenericContainer<?> connect, final String path) {
        return URI.create(OrderflowContainers.connectUrl(connect) + path);
    }

    private static HttpResponse<String> send(final HttpRequest request) {
        try {
            return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
