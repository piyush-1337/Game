package com.piyush.game.logic;

import com.piyush.game.GameTools.*;
import com.piyush.game.controllers.game.GameController;
import com.piyush.game.controllers.game.GameOverController;
import com.piyush.game.drawing.DrawingTools;
import com.piyush.game.drawing.WordBank;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameManager {
    private final ObservableList<Player> players;
    private final GameController gameController;
    private final ServerNetwork serverNetwork;
    private int turn = 0;
    private String wordToGuess;

    private final IntegerProperty timeLeft = new SimpleIntegerProperty(60);
    private Timeline timer;

    private final Set<String> correctGuessers = new HashSet<>();

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

                    if(correctGuessers.add(guess.playerName) && correctGuessers.size() == players.size()-1) {
                        Platform.runLater(() -> {
                            stopTurnTimer();
                            //on timer expired because in this case all the players have completed the round
                            onTimerExpired();
                            startTurnTimer();
                        });
                        correctGuessers.clear();
                    }

                    String message = guess.playerName + " GUESSED CORRECTLY!";
                    gameController.addChat(message, true);

                    //scoring
                    int score = 50 + (timeLeft.get()*5);

                    Platform.runLater(()->gameController.updateScore(guess.playerName, score));;

                    UpdateScoreCommand updateScoreCommand = new UpdateScoreCommand();
                    updateScoreCommand.playerName = guess.playerName;
                    updateScoreCommand.score = score;
                    serverNetwork.sendToAll(updateScoreCommand);

                } else {
                    //send to everyone else
                    GuessCommand forwardedGuess = new GuessCommand();
                    forwardedGuess.playerName = guess.playerName;
                    forwardedGuess.guessedWord = guess.guessedWord;
                    forwardedGuess.wordToGuess = this.wordToGuess;
                    serverNetwork.sendToAll(forwardedGuess);

                    gameController.addChat(guess.playerName + ": " + guess.guessedWord, false);
                }
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

            case ClearDrawingCommand clearDrawingCommand -> {
                gameController.clearDrawing();
                serverNetwork.sendToAll(clearDrawingCommand);
            }

            default -> throw new IllegalStateException("Unexpected value: " + command);
        }
    }


    public void beginGame(Label timerLabel) {

        List<Player> playersList = new ArrayList<>(this.players);

        PlayerListCommand playerListCommand = new PlayerListCommand();
        playerListCommand.players = playersList;

        this.wordToGuess = WordBank.getRandomWord();

        gameController.setWordToGuess(wordToGuess);
        gameController.setLabelText("Word: " + wordToGuess);
        gameController.enableDrawing();
        gameController.disableChat();

        WordToGuessCommand wordToGuessCommand = new WordToGuessCommand();
        wordToGuessCommand.correctWord = wordToGuess;

        serverNetwork.sendToAll(playerListCommand);
        serverNetwork.sendToAll(wordToGuessCommand);
        serverNetwork.sendToAll(new StartGameCommand());

        timerLabel.textProperty().bind(timeLeft.asString());
        startTurnTimer();
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
                onTimerExpired();
            }
        }));
        timer.setCycleCount(60);
        timer.play();
    }

    private void onTimerExpired() {
        players.get(turn).setisDrawing(false);
        turn++;
        if(turn == players.size()) {
            new Thread(()->{
                Stage stage = gameController.getStage();
                try{

                    serverNetwork.sendToAll(new GameOverCommand());
                    serverNetwork.stopGame();

                    Thread.sleep(100); //let the commands transfer and then switch the scene

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/piyush/game/scenes/GameOver.fxml"));
                    Parent root = loader.load();
                    GameOverController gameOverController = loader.getController();
                    gameOverController.setFinalScores(players);
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/com/piyush/game/styles/GameOverStyles.css").toExternalForm());
                    Platform.runLater(()->stage.setScene(scene));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();

        } else {
            correctGuessers.clear();

            players.get(turn).setisDrawing(true);
            gameController.disableDrawing();
            gameController.enableChat();
            gameController.setLabelText(players.get(turn).getPlayerName() + " is Drawing");

            this.wordToGuess = WordBank.getRandomWord();
            gameController.setWordToGuess(wordToGuess);

            YourTurnCommand yourTurnCommand = new YourTurnCommand();
            yourTurnCommand.wordToGuess = wordToGuess;
            serverNetwork.sentTo(yourTurnCommand, players.get(turn));

            NextTurnCommand nextTurnCommand = new NextTurnCommand();
            nextTurnCommand.playerName = players.get(turn).getPlayerName();
            nextTurnCommand.wordToGuess = wordToGuess;
            serverNetwork.sendToAllExcept(nextTurnCommand, players.get(turn));

            WordToGuessCommand wordToGuessCommand = new WordToGuessCommand();
            wordToGuessCommand.correctWord = wordToGuess;
            serverNetwork.sendToAll(wordToGuessCommand);

            Platform.runLater(this::startTurnTimer);
        }
    }

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

    public String getWordToGuess() {
        return this.wordToGuess;
    }
}
