package ua.dmytrolutsiuk.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum ComputationStatus {
    NOT_STARTED(0),
    PROCESSING(1),
    DONE(2);

    private static final Map<Integer, ComputationStatus> VALUE_TO_STATUS_MAP = new HashMap<>();

    static {
        for (ComputationStatus status : values()) {
            VALUE_TO_STATUS_MAP.put(status.value, status);
        }
    }

    private final int value;

    ComputationStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static ComputationStatus fromValue(int value) {
        ComputationStatus status = VALUE_TO_STATUS_MAP.get(value);
        if (status == null) {
            throw new IllegalArgumentException("Unknown ComputationStatus value: " + value);
        }
        return status;
    }
}
