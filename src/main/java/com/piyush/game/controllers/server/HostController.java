package com.piyush.game.controllers.server;

import com.piyush.game.controllers.game.GameController;
import com.piyush.game.network.client.Client;
import com.piyush.game.network.server.ServerNetwork;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HostController implements Initializable {

    private String username;
    private ServerNetwork serverNetwork;

    @FXML
    private ListView<Client> clientsListView;
    @FXML
    private Button startGameButton;

    public void setUsername(String username) {
        this.username = username;

        serverNetwork = new ServerNetwork();
        serverNetwork.startBroadCast(username);
        serverNetwork.startTCPServer();

        clientsListView.setItems(serverNetwork.getDiscoveredClients());

        Stage stage = (Stage) startGameButton.getScene().getWindow();
        stage.setOnCloseRequest(event -> {
            serverNetwork.stopTCPServer();
            serverNetwork.stopBroadCast();
            stage.close();
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Empty :(
    }

    public void startGame() throws IOException {
        serverNetwork.stopTCPServer();
        serverNetwork.stopBroadCast();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/piyush/game/scenes/Scribble.fxml"));
        Parent root = loader.load();
        GameController gameController = loader.getController();
        serverNetwork.setGameController(gameController);
        serverNetwork.startGame();
        gameController.setServerNetwork(serverNetwork);
        gameController.setiAmServer(true);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/piyush/game/styles/ScribbleStyles.css").toExternalForm());
        Stage stage = (Stage) startGameButton.getScene().getWindow();
        stage.setScene(scene);
    }
}
