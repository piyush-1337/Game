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
import java.util.ArrayList;
import java.util.List;

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

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), broadcastPort);
                    socket.send(packet);
                    System.out.println(broadcastMsg);

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
                        System.out.println("TCP Server started on port: " + gamePort);
                        break; // successfully bound
                    } catch (BindException be) {
                        System.out.println("Port " + port + " in use, trying next port...");
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
                // Ensure the ServerSocket is closed.
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

    public void sendToAllExcept(GameCommand command, Player excludedPlayer) {
        ObjectMapper mapper = new ObjectMapper();

        for (Client client : clientsList) {
            try {
                // Find the player corresponding to this client
                Player target = getPlayerBySocket(client.getSocket());

                // Skip the excluded player
                if (target == null || target.equals(excludedPlayer)) continue;

                // Send the command
                PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
                String json = mapper.writeValueAsString(command);
                out.println(json);

            } catch (IOException e) {
                System.err.println("Error sending to client " + client.getUsername() + ": " + e.getMessage());
            }
        }

        // Also check if the excluded player is not the server, and if host is playing, update local UI
        if (!excludedPlayer.isServer()) {
            // Optionally update GameController locally (if needed)
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

                        // Handle the message: deserialize JSON, pass to GameManager, etc.
                        System.out.println("Received from " + client.getUsername() + ": " + json);
                    }

                } catch (IOException e) {
                    System.err.println("Client disconnected: " + client.getUsername());
                }
            }).start();
        }
    }

    public void startGame() {
        players.add(new Player("Host", true, null)); // true = server player
        for (Client client : clientsList) {
            players.add(new Player(client.getUsername(), false,client.getSocket())); // false = not server
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
