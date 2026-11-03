package com.javarush.domain;

import java.util.Arrays;

/** Значения колонки film.rating (MySQL ENUM). "PG-13" и "NC-17" нельзя сделать именами констант, поэтому храним значение отдельно. */
public enum Rating {
    G("G"),
    PG("PG"),
    PG13("PG-13"),
    R("R"),
    NC17("NC-17");

    private final String value;

    Rating(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Rating fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(r -> r.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown rating: " + value));
    }
}
