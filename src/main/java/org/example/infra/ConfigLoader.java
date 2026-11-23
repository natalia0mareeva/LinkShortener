package org.example.infra;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private final Properties props = new Properties();

    public ConfigLoader() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Файл config.properties не найден");
            }
            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки конфигурации", e);
        }
    }

    public int getDefaultTtlMinutes() {
        return Integer.parseInt(props.getProperty("default.ttl.minutes", "60"));
    }

    public int getDefaultMaxClicks() {
        return Integer.parseInt(props.getProperty("default.max.clicks", "10"));
    }
}