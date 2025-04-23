package com.piyush.game.network.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.piyush.game.GameTools.GameCommand;
import com.piyush.game.GameTools.Player;
import com.piyush.game.controllers.game.GameController;
import com.piyush.game.logic.GameManager;
import com.piyush.game.network.client.Client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class ServerNetwork {

    private final int broadcastPort = 1337;
    private int gamePort = 5050;
    private boolean isBroadcasting = true;
    private boolean running = true;
    private String playerName;
    private boolean gameOver = false;
    private GameManager gameManager;
    private final ObjectMapper mapper = new ObjectMapper();
    private GameController gameController;

    private final ObservableList<Client> clientsList = FXCollections.observableArrayList();

    ObservableList<Player> players = FXCollections.observableArrayList();

    public void startBroadCast(String username) {
        playerName = username;

        new Thread(() -> {

            try(DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);

                while(isBroadcasting) {

                    String broadcastMsg = "Broadcasting-" + username + "-" + gamePort;
                    byte[] buffer = broadcastMsg.getBytes();
                    //calculate broadcast before using
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("10.25.95.255"), broadcastPort);
                    socket.send(packet);

                    Thread.sleep(3000);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

    private ServerSocket serverSocket;
    public void startTCPServer() {
        new Thread(() -> {
            try {

                int port = gamePort; // start trying from the base port (5050)
                while (true) {
                    try {
                        serverSocket = new ServerSocket(port);
                        this.gamePort = port; // update the gamePort with the available port
                        break; // successfully bound
                    } catch (BindException be) {
                        port++; // try next port
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                while (running) {
                    Socket clientSocket = serverSocket.accept();  // Blocking call, waits for connection

                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String username = in.readLine();

                    Client client = new Client(clientSocket, username);

                    // Check for duplicates before adding
                    if (!clientsList.contains(client)) {
                        Platform.runLater(() -> clientsList.add(client));
                    }
                }
            } catch (IOException e) {
                if(running) {
                    throw new RuntimeException(e);
                }
            } finally {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void sendToAll(GameCommand cmd) {
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(cmd);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        for (Client client : clientsList) {
            try {
                PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
                out.println(json);
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }

    public void sentTo(GameCommand cmd, Player player) {
        try {
            PrintWriter out = new PrintWriter(player.getSocket().getOutputStream(), true);
            String json = mapper.writeValueAsString(cmd);
            out.println(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendToAllExcept(GameCommand command, Player excludedPlayer) {
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        for (Client client : clientsList) {
            try {
                Player target = getPlayerBySocket(client.getSocket());

                if (target == null || target.equals(excludedPlayer)) continue;

                PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
                out.println(json);

            } catch (IOException e) {
                System.err.println("Error sending to client " + client.getUsername() + ": " + e.getMessage());
            }
        }
    }

    public void receiveMessageFromAll() {
        for (Client client : clientsList) {
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(client.getSocket().getInputStream())
                    );

                    String json;
                    while ((json = in.readLine()) != null && !gameOver) {

                        GameCommand cmd = mapper.readValue(json,GameCommand.class);
                        gameManager.handleCommand(cmd,getPlayerBySocket(client.getSocket()));

                    }

                } catch (IOException e) {
                    System.err.println("Client disconnected: " + client.getUsername());
                }
            }).start();
        }
    }

    public void startGame() {
        players.add(new Player(playerName, true, null));
        for (Client client : clientsList) {
            players.add(new Player(client.getUsername(), false,client.getSocket()));
        }

        gameManager = new GameManager(players, this, gameController);
        gameManager.beginGame(gameController.getTimerLabel());
    }

    public Player getPlayerBySocket(Socket socket) {
        for (Player player : players) {
            if (!player.isServer() && player.getSocket().equals(socket)) {
                return player;
            }
        }
        return null;
    }

    public ObservableList<Client> getDiscoveredClients() {
        return clientsList;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    public void stopBroadCast() {
        isBroadcasting = false;
    }

    public void stopTCPServer() {
        running = false;
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stopGame() {
        gameOver = true;
    }

    public String getPlayerName() {
        return playerName;
    }

    public ObservableList<Player> getPlayersList() {
        return players;
    }

}
