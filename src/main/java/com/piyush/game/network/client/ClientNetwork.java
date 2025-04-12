package com.piyush.game.network.client;

import com.piyush.game.network.server.Server;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

public class ClientNetwork {

    private final int broadcastPort = 1337;
    private int gamePort = 5050;
    private boolean listening = true;

    private final ObservableList<Server> discoveredServers = FXCollections.observableArrayList();
    private Server server;

    public void startListeningForBroadcast() {
        new Thread(() -> {

            try(DatagramSocket socket = new DatagramSocket(broadcastPort)) {
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

    public void setServer(int index, String username) throws IOException {
        server = discoveredServers.get(index);

        Socket socket = new Socket(server.getIp(),server.getGamePort());
        server.setSocket(socket);

        //This username will be added to the listview of Host
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(username);
    }

//    public void sendMessageToServer(String message) {
//        try {
//            PrintWriter out = new PrintWriter(server.getSocket().getOutputStream(), true);
//            out.println(message);
//            out.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public void setGamePort(int gamePort) {
        this.gamePort = gamePort;
    }

    public ObservableList<Server> getDiscoveredServers() {
        return discoveredServers;
    }

    public void stopListeningToBroadcast() {
        listening = false;
    }

}
