package ua.dmytrolutsiuk.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum CommandResponseType {
    OK(0),
    ERROR(1);

    private static final Map<Integer, CommandResponseType> VALUE_TO_TYPE_MAP = new HashMap<>();

    static {
        for (CommandResponseType type : values()) {
            VALUE_TO_TYPE_MAP.put(type.value, type);
        }
    }

    private final int value;

    CommandResponseType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static CommandResponseType fromValue(int value) {
        CommandResponseType type = VALUE_TO_TYPE_MAP.get(value);
        if (type == null) {
            throw new IllegalArgumentException("Unknown CommandResponseType value: " + value);
        }
        return type;
    }
}
