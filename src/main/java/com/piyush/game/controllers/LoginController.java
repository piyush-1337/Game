package com.piyush.game.controllers;

import com.piyush.game.controllers.client.JoinController;
import com.piyush.game.controllers.server.HostController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField username;

    @FXML
    private RadioButton hostRadioButton;

    public void proceed() throws IOException {
        if(hostRadioButton.isSelected() && !username.getText().isBlank()) {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/piyush/game/scenes/Host.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) username.getScene().getWindow();
            stage.setScene(new Scene(root));

            HostController hostController = loader.getController();
            hostController.setUsername(username.getText().trim());

        } else if(!username.getText().isBlank()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/piyush/game/scenes/Join.fxml"));
            Parent root = loader.load();

            JoinController joinController = loader.getController();
            joinController.setUsername(username.getText().trim());

            Stage stage = (Stage) username.getScene().getWindow();
            stage.setScene(new Scene(root));

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Please enter a valid username");
            alert.showAndWait();
        }
    }
}