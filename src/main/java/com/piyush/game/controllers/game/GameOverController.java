package com.piyush.game.controllers.game;

import com.piyush.game.GameTools.Player;
import com.piyush.game.network.client.ClientNetwork;
import com.piyush.game.network.server.ServerNetwork;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class GameOverController {

    @FXML
    private ListView<Player> finalScores;

    public void setFinalScores(ObservableList<Player> players) {
        this.finalScores.setItems(players);
    }
}
