package com.piyush.game.network.client;

import java.net.Socket;
import java.util.Objects;

public class Client {

    private Socket socket;
    private String username;

    public Client(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Override equals() to consider two Client objects equal if they have the same username
    // and come from the same remote socket address.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client)) return false;
        Client client = (Client) o;
        return Objects.equals(username, client.username) &&
                Objects.equals(getRemoteAddress(), client.getRemoteAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, getRemoteAddress());
    }

    // Utility method to extract the client's remote socket address.
    public String getRemoteAddress() {
        return socket != null && socket.getRemoteSocketAddress() != null
                ? socket.getRemoteSocketAddress().toString() : "";
    }

    // Useful for debugging and for display in UI components like ListView.
    @Override
    public String toString() {
        return username;
    }
}