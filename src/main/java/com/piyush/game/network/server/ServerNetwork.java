package com.piyush.game.network.server;

import com.piyush.game.network.client.Client;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerNetwork {

    private final int broadcastPort = 1337;
    private int gamePort = 5050;
    private boolean isBroadcasting = true;
    private boolean running = true;
    private String playerName;
    private boolean gameOver = false;

    private final ObservableList<Client> clientsList = FXCollections.observableArrayList();

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

    public void sendMessageToAll(String message) {
        String broadcastMsg = playerName + "-" + message;

        for (Client client : clientsList) {
            try {
                PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
                out.println(broadcastMsg);
                out.close();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }

    public void receiveMessageFromAll() {

            new Thread(() -> {

                ExecutorService executor = Executors.newFixedThreadPool(clientsList.size());

                while (!gameOver) {
                    for (Client client : clientsList) {

                        executor.execute(() -> {

                            try {
                                //TODO: Use JSON or something this wont work; too complicated
                                BufferedReader in = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream()));
                                String message = in.readLine();
                                sendMessageToAll(message);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }

                }

            }).start();

    }

    public ObservableList<Client> getDiscoveredClients() {
        return clientsList;
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

}
