package com.piyush.game.GameTools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.net.Socket;
import java.util.Objects;

@JsonIgnoreProperties("socket")
public class Player {

    private String playerName;
    private boolean isServer;
    private Socket socket;
    private int score;
    private boolean isDrawing;

    public Player() {}

    public Player(String playerName, boolean isServer, Socket socket) {
        this.playerName = playerName;
        this.isServer = isServer;
        this.socket = socket;
        this.score = 0;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isisDrawing() {
        return isDrawing;
    }

    public void setisDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
    }

    public boolean isServer() {
        return isServer;
    }

    public void setServer(boolean server) {
        isServer = server;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public String toString() {
        return playerName + "-" + score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player player)) return false;
        return score == player.score && isDrawing == player.isDrawing && isServer == player.isServer && Objects.equals(playerName, player.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, score, isDrawing, isServer);
    }

}
