package ua.dmytrolutsiuk.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum CommandDataKey {
    MATRIX_SIZE(1),
    THREADS_AMOUNT(2),
    STATUS(3),
    EXECUTION_TIME_MS(4),
    MATRIX(5),
    MESSAGE(6);

    private static final Map<Integer, CommandDataKey> VALUE_TO_KEY_MAP = new HashMap<>();

    static {
        for (CommandDataKey key : values()) {
            VALUE_TO_KEY_MAP.put(key.value, key);
        }
    }

    private final int value;

    CommandDataKey(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static CommandDataKey fromValue(int value) {
        CommandDataKey key = VALUE_TO_KEY_MAP.get(value);
        if (key == null) {
            throw new IllegalArgumentException("Unknown CommandDataKey value: " + value);
        }
        return key;
    }
}
