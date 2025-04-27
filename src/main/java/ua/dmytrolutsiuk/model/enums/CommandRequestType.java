package ua.dmytrolutsiuk.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum CommandRequestType {
    INITIALIZE(0),
    START(1),
    GET_STATUS(2),
    GET_RESULT(3);

    private static final Map<Integer, CommandRequestType> VALUE_TO_TYPE_MAP = new HashMap<>();

    static {
        for (CommandRequestType type : values()) {
            VALUE_TO_TYPE_MAP.put(type.value, type);
        }
    }

    private final int value;

    CommandRequestType(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static CommandRequestType fromValue(int value) {
        CommandRequestType type = VALUE_TO_TYPE_MAP.get(value);
        if (type == null) {
            throw new IllegalArgumentException("Unknown CommandRequestType value: " + value);
        }
        return type;
    }
}

