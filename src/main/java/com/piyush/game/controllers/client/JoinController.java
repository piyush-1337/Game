package com.piyush.game.controllers.client;

import com.piyush.game.controllers.game.GameController;
import com.piyush.game.network.client.ClientNetwork;
import com.piyush.game.network.server.Server;
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

public class JoinController implements Initializable {

    private String username;
    private ClientNetwork clientNetwork;

    @FXML
    private ListView<Server> serversListView;
    @FXML
    private Button joinButton;

    public void setUsername(String username) {
        this.username = username;

        clientNetwork = new ClientNetwork();
        clientNetwork.startListeningForBroadcast();

        serversListView.setItems(clientNetwork.getDiscoveredServers());
        serversListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                joinServer();
            }
        });

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Empty :(
    }

    public void joinServer() {
        int index = serversListView.getSelectionModel().getSelectedIndex();
        String serverName = clientNetwork.getDiscoveredServers().get(index).getUsername();

        try {
            clientNetwork.setPlayerName(username);
            clientNetwork.setServer(index, serverName);
            clientNetwork.stopListeningToBroadcast();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/piyush/game/scenes/Scribble.fxml"));
            Parent root = loader.load();
            GameController gameController = loader.getController();
            gameController.setiAmServer(false);
            clientNetwork.startListeningFromServer();
            gameController.setClientNetwork(clientNetwork);
            gameController.setPlayerName(username);

            gameController.disableDrawing();
            gameController.enableChat();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/piyush/game/styles/ScribbleStyles.css").toExternalForm());
            Stage stage = (Stage) serversListView.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
