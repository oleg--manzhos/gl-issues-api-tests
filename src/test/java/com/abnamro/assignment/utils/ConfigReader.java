package com.abnamro.assignment.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input =
                     ConfigReader.class.getClassLoader()
                             .getResourceAsStream("config.properties")) {

            PROPERTIES.load(input);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String get(String key) {
        String value = System.getProperty(
                key,
                PROPERTIES.getProperty(key)
        );

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Configuration property is missing or empty: " + key
            );
        }

        return value;
    }
}
