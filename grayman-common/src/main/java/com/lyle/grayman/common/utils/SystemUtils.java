package com.lyle.grayman.common.utils;

public final class SystemUtils {
    private SystemUtils() {
    }

    public static String getPropetyOrEnv(String key) {
        String value = System.getProperty(key);

        if (value == null) {
            value = System.getenv(key);
        }

        return value;
    }
}
