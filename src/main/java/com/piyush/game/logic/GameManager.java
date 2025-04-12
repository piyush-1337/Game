package com.piyush.game.logic;

import com.piyush.game.GameTools.DrawCommand;
import com.piyush.game.GameTools.GameCommand;
import com.piyush.game.GameTools.GuessCommand;
import com.piyush.game.GameTools.Player;
import com.piyush.game.controllers.game.GameController;
import com.piyush.game.network.server.Server;
import com.piyush.game.network.server.ServerNetwork;

import java.util.List;

public class GameManager {
    private List<Player> players;
    private GameController gameController;
    private ServerNetwork serverNetwork;

    public GameManager(List<Player> players, ServerNetwork serverNetwork, GameController gameController) {
        this.players = players;
        this.gameController = gameController;
        this.serverNetwork = serverNetwork;
    }

    public void handleCommand(GameCommand command, Player sender) {
        switch (command) {
            case GuessCommand guess -> {
                /* check word, update score */
                serverNetwork.sendMessageToAll(gameController.getWordToGuess());
            }
            case DrawCommand draw -> {
                // forward to others (except sender)
                serverNetwork.sendToAllExcept(draw, sender);
            }
            // more cases...
            default -> throw new IllegalStateException("Unexpected value: " + command);
        }
    }


    public void beginGame() {



    }
}
