package com.piyush.game.GameTools;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GuessCommand.class, name = "guess"),
        @JsonSubTypes.Type(value = DrawCommand.class, name = "draw"),
        @JsonSubTypes.Type(value = WordToGuessCommand.class, name = "correctWord"),
        @JsonSubTypes.Type(value = UpdateScoreCommand.class, name = "updateScore"),
        @JsonSubTypes.Type(value = NextTurnCommand.class, name = "nextTurn"),
        @JsonSubTypes.Type(value = PlayerListCommand.class, name = "playerList"),
        @JsonSubTypes.Type(value = StartGameCommand.class, name = "startGame")
        // Add other command types here
})
public abstract class GameCommand {
    public String type;
}
