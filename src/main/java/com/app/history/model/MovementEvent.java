package com.app.history.model;

import java.util.Locale;

public enum MovementEvent {
    MOVEMENT_IN("BEWGZU"),
    DELETE("LOESCH"),
    MOVEMENT_OUT("BEWGAB"),
    MOVEMENT_NEUTRAL("BEWGNG"),
    GOODS_RECEIPT("WAREIN"),
    BATCH_CORRECTION_IN("MGKOZU"),
    BATCH_CORRECTION_OUT("MGKOAB"),
    INVENTORY_COUNT("INVZHL");

    private final String code;

    MovementEvent(String code) {
        this.code = code;
    }

    public static MovementEvent fromCode(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty() || normalized.equals("_".repeat(20)) || normalized.equals("_".repeat(10))) {
            return null;
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        for (MovementEvent event : values()) {
            if (event.code.equals(upper)) {
                return event;
            }
        }
        throw new IllegalArgumentException(normalized);
    }

    public String code() {
        return code;
    }
}
