package com.piyush.game.network.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piyush.game.GameTools.*;
import com.piyush.game.controllers.game.GameController;
import com.piyush.game.controllers.game.GameOverController;
import com.piyush.game.drawing.DrawingTools;
import com.piyush.game.network.server.Server;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class ClientNetwork {

    private final int broadcastPort = 1337;
    private int gamePort = 5050;
    private boolean listening = true;
    public String playerName;

    private final ObservableList<Server> discoveredServers = FXCollections.observableArrayList();
    private Server server;

    private GameController gameController;

    private boolean gameOver = false;

    public void startListeningForBroadcast() {
        new Thread(() -> {

            try(DatagramSocket socket = new DatagramSocket(broadcastPort, InetAddress.getByName("0.0.0.0"))) {
                socket.setSoTimeout(5000);
                byte[] buffer = new byte[1024];

                while (listening){

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());
                    String[] parts = message.split("-");

                    if(parts[0].equals("Broadcasting")){

                        this.gamePort = Integer.parseInt(parts[2]);
                        Server newServer = new Server(packet.getAddress(), parts[1], gamePort);
                        if(!discoveredServers.contains(newServer)){
                            Platform.runLater(() -> discoveredServers.add(newServer));
                        }
                    }

                }

            } catch (Exception e){
                throw new RuntimeException(e);
            }

        }).start();
    }

    public void startListeningFromServer() {
        new Thread(() -> {

            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(server.getSocket().getInputStream())
                );
                ObjectMapper mapper = new ObjectMapper();
                String json;
                while ((json = reader.readLine()) != null && !gameOver) {
                    GameCommand command = mapper.readValue(json, GameCommand.class);
                    handleCommand(command);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

    public void handleCommand(GameCommand command) {

        switch (command) {
            case GuessCommand guess -> {

                gameController.addChat(guess.playerName + ": " + guess.guessedWord, false);
            }
            case DrawCommand dc -> {
                DrawingTools.LineCommand line =
                        new DrawingTools.LineCommand(dc.startX, dc.startY, dc.endX, dc.endY);

                Platform.runLater(() -> gameController.processDrawCommand(line));
            }
            case WordToGuessCommand wordToGuessCommand -> {
                gameController.setWordToGuess(wordToGuessCommand.correctWord);
            }

            case UpdateScoreCommand updateScore -> {
                gameController.addChat(updateScore.playerName + " GUESSED CORRECTLY!", true);
                Platform.runLater(() -> gameController.updateScore(updateScore.playerName, updateScore.score));
            }

            case PlayerListCommand playerListCommand -> {
                gameController.setScoreList(FXCollections.observableArrayList(playerListCommand.players));
            }

            case StartGameCommand startGameCommand -> {
                startGame();
            }

            case ClearDrawingCommand clearDrawingCommand -> {
                gameController.clearDrawing();
            }

            case YourTurnCommand yourTurnCommand -> {
                gameController.setWordToGuess(yourTurnCommand.wordToGuess);
                gameController.setLabelText("Word: " + gameController.getWordToGuess());
                gameController.disableChat();
                gameController.enableDrawing();

                Platform.runLater(this::startTurnTimer);;
            }

            case NextTurnCommand nextTurnCommand -> {
                gameController.setWordToGuess(nextTurnCommand.wordToGuess);
                gameController.setLabelText(nextTurnCommand.playerName + " is Drawing");
                gameController.disableDrawing();
                gameController.enableChat();

                Platform.runLater(this::startTurnTimer);
            }

            case GameOverCommand gameOverCommand -> {
                Stage stage = gameController.getStage();
                try{
                    Thread.sleep(100);

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/piyush/game/scenes/GameOver.fxml"));
                    Parent root = loader.load();
                    GameOverController gameOverController = loader.getController();
                    gameOverController.setFinalScores(gameController.getScoreList());
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/com/piyush/game/styles/GameOverStyles.css").toExternalForm());
                    Platform.runLater(()->stage.setScene(scene));
                    stopGame();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            default -> throw new IllegalStateException("Unexpected value: " + command);
        }

    }

    //Game logic from here
    IntegerProperty timeLeft = new SimpleIntegerProperty(60);
    Timeline timer;

    public void startGame() {

        gameController.setLabelText(server.getUsername() + " is Drawing");

        Platform.runLater(() -> gameController.getTimerLabel().textProperty().bind(timeLeft.asString()));
        startTurnTimer();


        //this is not related but couldn't find a spot to put this so here it is
        Stage stage = gameController.getStage();
        stage.setOnCloseRequest(event -> {
            stopListeningToBroadcast();
            stage.close();
        });

    }

    public void startTurnTimer() {
        timeLeft.set(60);

        if (timer != null) {
            timer.stop();
        }

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft.set(timeLeft.get() - 1);
            if (timeLeft.get() <= 0) {
                timer.stop();
            }
        }));
        timer.setCycleCount(60);
        timer.play();
    }

    public void setServer(int index, String username) throws IOException {
        server = discoveredServers.get(index);

        Socket socket = new Socket(server.getIp(),server.getGamePort());
        server.setSocket(socket);

        //This username will be added to the listview of Host
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(playerName);
    }

    public void sendCommandToServer(GameCommand command) {
        try {
            PrintWriter out = new PrintWriter(server.getSocket().getOutputStream(), true);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(command);
            out.println(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setGamePort(int gamePort) {
        this.gamePort = gamePort;
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public ObservableList<Server> getDiscoveredServers() {
        return discoveredServers;
    }

    public void stopListeningToBroadcast() {
        listening = false;
    }

    public void stopGame() {
        gameOver = true;
    }

}
