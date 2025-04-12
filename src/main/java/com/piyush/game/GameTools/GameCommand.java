package com.piyush.game.GameTools;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GuessCommand.class, name = "guess"),
        @JsonSubTypes.Type(value = DrawCommand.class, name = "draw"),
        // Add other command types here
})
public abstract class GameCommand {
    public String type;
}
