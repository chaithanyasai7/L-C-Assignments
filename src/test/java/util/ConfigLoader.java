package src.test.java.util;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties props = new Properties();

    static {
        try {
            props.load(Objects.requireNonNull(
                    ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")
            ));
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Could not load config.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
