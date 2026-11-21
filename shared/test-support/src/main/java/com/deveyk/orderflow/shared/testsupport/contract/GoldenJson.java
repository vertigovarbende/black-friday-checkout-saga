package com.deveyk.orderflow.shared.testsupport.contract;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public final class GoldenJson {

    private GoldenJson() {
    }

    public static String read(final String contractName) {

        final String path = "contracts/" + contractName + ".json";

        try (InputStream in = GoldenJson.class.getClassLoader().getResourceAsStream(path)) {

            if (in == null)
                throw new IllegalArgumentException("Contract not found on classpath: " + path);

            return new String(in.readAllBytes(), StandardCharsets.UTF_8);

        } catch (final IOException e) {

            throw new UncheckedIOException(e);

        }
    }

    public static void assertMatches(final String contractName, final String actualJson) {
        try {
            JSONAssert.assertEquals(read(contractName), actualJson, JSONCompareMode.STRICT);
        } catch (final JSONException e) {
            // JSONAssert'in checked exception'ı: iki taraftan biri geçerli JSON değil.
            throw new IllegalArgumentException("Invalid JSON while comparing contract " + contractName, e);
        }
    }
}
