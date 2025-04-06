package com.piyush.game.controllers.game;

import com.piyush.game.drawing.DrawingTools;
import com.piyush.game.network.client.ClientNetwork;
import com.piyush.game.network.server.ServerNetwork;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML
    private BorderPane borderPane;
    @FXML
    private ListView<String> listView;
    @FXML
    private Canvas canvas;
    @FXML
    private Label label;
    @FXML
    private VBox vBox;
    @FXML
    private Button sendButton;
    @FXML
    private Button clearButton;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private HBox hBox;
    @FXML
    private TextField textField;

    private DrawingTools drawingTools;

    private boolean drawing = false;

    private String wordToGuess;

    private ServerNetwork serverNetwork;

    private ClientNetwork clientNetwork;

    private boolean iAmServer = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        drawingTools = new DrawingTools(canvas);


        /* **********************************CANVAS CODE************************************************** */
        // Bind Canvas width to available horizontal space
        canvas.widthProperty().bind(
                borderPane.widthProperty()
                        .subtract(listView.widthProperty())
                        .subtract(scrollPane.widthProperty())
        );

        // Bind Canvas height to available vertical space
        canvas.heightProperty().bind(
                borderPane.heightProperty()
                        .subtract(label.heightProperty())
                        .subtract(hBox.heightProperty())
        );

        canvas.widthProperty().addListener((obs, oldVal, newVal) -> drawingTools.redraw());
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> drawingTools.redraw());
        drawingTools.redraw();

        canvas.setOnMousePressed(e -> {
            drawing = true;
            drawingTools.lastX = e.getX();
            drawingTools.lastY = e.getY();
        });

        canvas.setOnMouseDragged(e -> {
            if (drawing) {
                drawingTools.drawHistory.add(new DrawingTools.LineCommand(drawingTools.lastX, drawingTools.lastY, e.getX(), e.getY()));
                drawingTools.lastX = e.getX();
                drawingTools.lastY = e.getY();
                drawingTools.redraw();
            }
        });
        /* *********************************************************************************************** */

        /* **********************************MESSAGE CODE************************************************* */
        Stage stage = (Stage) sendButton.getScene().getWindow();

        //automatically scrolls the scrollpane when there are more messages than scrollpane height
        vBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue(newValue.doubleValue());
        });

        sendButton.setOnAction(e -> {sendMessage();});
        /* *********************************************************************************************** */


        /* **********************************ADDITIONAL CODE********************************************** */
        clearButton.setOnAction(e -> {drawingTools.clearCanvas();});
    }

    public void sendMessage() {

        if(!textField.getText().isBlank()) {

            String message = textField.getText();

            if(message.equals(wordToGuess)) {
                //TODO: correctly guessed the word add points
            }
            else {
                //send the message to the chat box
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER_LEFT);
                hBox.setPadding(new Insets(5,5,5,10));

                Text text = new Text(message);
                text.setFill(Color.BLACK);
                text.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
                text.setSmooth(false);

                TextFlow textFlow = new TextFlow(text);
                textFlow.setStyle("-fx-color: rgb(239,242,255);" +
                        "-fx-background-color: rgb(15,125,242);" +
                        "-fx-background-radius: 20px;");
                textFlow.setPadding(new Insets(5,10,5,10));
                text.setFill(Color.color(0.934,0.945,0.996));

                hBox.getChildren().add(textFlow);
                vBox.getChildren().add(hBox);

                if(iAmServer) {
                    serverNetwork.sendMessageToAll(message);
                }
            }
        }
    }

    public void setServerNetwork(ServerNetwork serverNetwork) {
        this.serverNetwork = serverNetwork;
    }

    public void setClientNetwork(ClientNetwork clientNetwork) {
        this.clientNetwork = clientNetwork;
    }

    public void setiAmServer(boolean iAmServer) {
        this.iAmServer = iAmServer;
    }

}
