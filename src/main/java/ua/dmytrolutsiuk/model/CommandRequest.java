package ua.dmytrolutsiuk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import ua.dmytrolutsiuk.model.enums.CommandDataKey;
import ua.dmytrolutsiuk.model.enums.CommandRequestType;

import java.util.Map;

public record CommandRequest(
        @JsonProperty("0") CommandRequestType commandRequestType,
        @JsonProperty("1") Map<CommandDataKey, Object> data
) {

}