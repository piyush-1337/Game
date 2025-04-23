package com.piyush.game.controllers.game;

import com.piyush.game.GameTools.*;
import com.piyush.game.drawing.DrawingTools;
import com.piyush.game.network.client.ClientNetwork;
import com.piyush.game.network.server.ServerNetwork;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML
    private BorderPane borderPane;
    @FXML
    private ListView<Player> listView;
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
    @FXML
    private Label timerLabel;

    private DrawingTools drawingTools;

    private boolean drawing = false;

    private String wordToGuess;

    private ServerNetwork serverNetwork;

    private ClientNetwork clientNetwork;

    private boolean iAmServer = false;

    private ObservableList<Player> scoreList = FXCollections.observableArrayList();

    private String playerName;

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

        /* *********************************************************************************************** */

        /* **********************************MESSAGE CODE************************************************* */
        //automatically scrolls the scrollpane when there are more messages than scrollpane height
        vBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue(newValue.doubleValue());
        });

        sendButton.setOnAction(e -> {sendMessage();});
        /* *********************************************************************************************** */


        /* **********************************ADDITIONAL CODE********************************************** */

        if(!iAmServer){
            timerLabel.setText("Waiting for server...");
        }
    }

    public void sendMessage() {

        if (!textField.getText().isBlank()) {
            String word = textField.getText().trim();
            textField.clear();

            GuessCommand guessCommand = new GuessCommand();
            guessCommand.playerName = playerName;
            guessCommand.guessedWord = word;

            if (iAmServer) {
                // Process the guess locally for the server's GameManager
                Player serverPlayer = serverNetwork.getGameManager().getPlayers().getFirst();
                serverNetwork.getGameManager().handleCommand(guessCommand, serverPlayer);
            } else {
                clientNetwork.sendCommandToServer(guessCommand);
            }
        }
    }

    public void addChat(String message, boolean isCorrect) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5,5,5,10));

        Text text = new Text(message);
        text.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        text.setFill(Color.web("#2c3e50")); // Using existing dark text color

        TextFlow textFlow = new TextFlow(text);
        textFlow.getStyleClass().add(isCorrect ? "correct-message" : "incorrect-message");
        textFlow.setPadding(new Insets(5,10,5,10));

        Platform.runLater(() -> {
            hBox.getChildren().add(textFlow);
            vBox.getChildren().add(hBox);
        });
    }

    public void enableDrawing() {
        canvas.setOnMousePressed(e -> {
            drawing = true;
            drawingTools.lastX = e.getX();
            drawingTools.lastY = e.getY();
        });

        canvas.setOnMouseDragged(e -> {
            if (drawing) {

                DrawCommand drawCommand = new DrawCommand();
                drawCommand.startX = drawingTools.lastX;
                drawCommand.startY = drawingTools.lastY;
                drawCommand.endX = e.getX();
                drawCommand.endY = e.getY();

                if(iAmServer) {
                    serverNetwork.sendToAll(drawCommand);
                } else {
                    clientNetwork.sendCommandToServer(drawCommand);
                }

                drawingTools.drawHistory.add(new DrawingTools.LineCommand(drawingTools.lastX, drawingTools.lastY, e.getX(), e.getY()));
                drawingTools.lastX = e.getX();
                drawingTools.lastY = e.getY();
                drawingTools.redraw();
            }
        });

        canvas.setOnMouseReleased(e -> drawing = false);

        clearButton.setOnAction(e -> {
            clearDrawing();

            if(iAmServer) {
                serverNetwork.sendToAll(new ClearDrawingCommand());
            } else {
                clientNetwork.sendCommandToServer(new ClearDrawingCommand());
            }
        });

        clearButton.setDisable(false);
    }

    public void disableDrawing() {
        canvas.setOnMousePressed(null);
        canvas.setOnMouseDragged(null);
        canvas.setOnMouseReleased(null);

        clearButton.setDisable(true);
    }

    public void enableChat() {
        textField.setDisable(false);
        sendButton.setDisable(false);
    }

    public void disableChat() {
        textField.setDisable(true);
        sendButton.setDisable(true);
    }

    public void updateScore(String updateName, int score) {
        for(Player p : scoreList) {
            if(p.getPlayerName().equals(updateName)) {
                p.setScore(p.getScore()+score);
                break;
            }
        }
        listView.refresh();
    }

    public void clearDrawing() {
        drawingTools.clearCanvas();
    }

    private final Timeline labelAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, e -> {
                label.getStyleClass().add("text-changed");
            }),
            new KeyFrame(Duration.millis(1500), e -> {
                label.getStyleClass().remove("text-changed");
            })
    );
    public void setLabelText(String text) {
        Platform.runLater(() -> {
            label.setText(text);
            labelAnimation.stop();
            labelAnimation.play();
        });
    }

    public void processDrawCommand(DrawingTools.LineCommand line) {
        drawingTools.drawHistory.add(line);
        drawingTools.redraw();
    }

    public void setServerNetwork(ServerNetwork serverNetwork) {
        this.serverNetwork = serverNetwork;
    }

    public void setClientNetwork(ClientNetwork clientNetwork) {
        this.clientNetwork = clientNetwork;
        clientNetwork.setGameController(this);
    }

    public void setiAmServer(boolean iAmServer) {
        this.iAmServer = iAmServer;
    }

    public String getWordToGuess() {
        return wordToGuess;
    }

    public void setWordToGuess(String wordToGuess) {
        this.wordToGuess = wordToGuess;
    }

    public DrawingTools getDrawingTools() {
        return drawingTools;
    }

    public Label getTimerLabel() {
        return timerLabel;
    }

    public Stage getStage() {
        return (Stage) sendButton.getScene().getWindow();
    }

    public ObservableList<Player> getScoreList() {
        return scoreList;
    }

    public void setScoreList(ObservableList<Player> players){
        this.scoreList = players;
        listView.setItems(scoreList);
    }

    public void setPlayerName(String name){
        this.playerName = name;
    }
}
