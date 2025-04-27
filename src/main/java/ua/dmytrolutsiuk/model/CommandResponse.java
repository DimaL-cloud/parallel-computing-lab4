package ua.dmytrolutsiuk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import ua.dmytrolutsiuk.model.enums.CommandDataKey;
import ua.dmytrolutsiuk.model.enums.CommandResponseType;

import java.util.Map;

public record CommandResponse(
        @JsonProperty("0") CommandResponseType commandResponseType,
        @JsonProperty("1") Map<CommandDataKey, Object> data
) {
}
