package com.piyush.game.logic;

import com.piyush.game.GameTools.*;
import com.piyush.game.controllers.game.GameController;
import com.piyush.game.drawing.DrawingTools;
import com.piyush.game.network.server.ServerNetwork;
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
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private final ObservableList<Player> players;
    private final GameController gameController;
    private final ServerNetwork serverNetwork;
    private int turn;
    private String wordToGuess;

    private final IntegerProperty timeLeft = new SimpleIntegerProperty(60);
    private Timeline timer;

    public GameManager(ObservableList<Player> players, ServerNetwork serverNetwork, GameController gameController) {
        this.players = players;
        this.gameController = gameController;
        this.serverNetwork = serverNetwork;
        gameController.setScoreList(FXCollections.observableArrayList(this.players));
    }

    public void handleCommand(GameCommand command, Player sender) {
        switch (command) {
            case GuessCommand guess -> {

                if(guess.guessedWord.equalsIgnoreCase(wordToGuess)) {



                }

                gameController.addChat(guess.playerName + "-" + guess.guessedWord);

                //send to everyone else
                serverNetwork.sendToAllExcept(guess, sender);
            }
            case DrawCommand dc -> {
                DrawingTools.LineCommand line =
                        new DrawingTools.LineCommand(dc.startX, dc.startY, dc.endX, dc.endY);

                Platform.runLater(() ->
                        gameController.processDrawCommand(line)
                );

                // forward to others (except sender)
                serverNetwork.sendToAllExcept(dc, sender);
            }

            default -> throw new IllegalStateException("Unexpected value: " + command);
        }
    }


    public void beginGame(Label timerLabel) {

        List<Player> playersList = new ArrayList<>(this.players);

        PlayerListCommand playerListCommand = new PlayerListCommand();
        playerListCommand.players = playersList;

        gameController.setWordToGuess(wordToGuess);
        gameController.enableDrawing();

        WordToGuessCommand wordToGuessCommand = new WordToGuessCommand();
        wordToGuessCommand.correctWord = wordToGuess;

        serverNetwork.sendToAll(playerListCommand);
        serverNetwork.sendToAll(wordToGuessCommand);
        serverNetwork.sendToAll(new StartGameCommand());

        timerLabel.textProperty().bind(timeLeft.asString());
        startTurnTimer();
    }

    public void startTurnTimer() {
        // reset
        timeLeft.set(60);

        // if an old timer is running, stop it
        if (timer != null) {
            timer.stop();
        }

        // every 1 second, decrement timeLeft by 1
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft.set(timeLeft.get() - 1);
            if (timeLeft.get() <= 0) {
                timer.stop();
                onTimerExpired();
            }
        }));
        timer.setCycleCount(60);  // run 60 times
        timer.play();
    }

    private void onTimerExpired() {
        // e.g. disable drawing/guessing, notify server, advance turn…
        System.out.println("Time’s up!");
        players.get(turn).setisDrawing(false);
        turn++;
        if(turn == players.size()) {
            //show a screen of player rankings and exit the game
            Stage stage = gameController.getStage();
            try{
            Parent root = FXMLLoader.load(getClass().getResource("/com/piyush/game/scenes/GameOver.fxml"));
            stage.setScene(new Scene(root));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            players.get(turn).setisDrawing(true);
        }
        // TODO: send an EndTurnCommand or invoke GameManager.nextTurn()
    }

    // call this if you ever need to cancel early (e.g. game ended)
    public void stopTurnTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    public IntegerProperty getTimeLeft() {
        return timeLeft;
    }

    public ObservableList<Player> getPlayers() {
        return players;
    }
}
