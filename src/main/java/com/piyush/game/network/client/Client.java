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

    public String getRemoteAddress() {
        return socket != null && socket.getRemoteSocketAddress() != null
                ? socket.getRemoteSocketAddress().toString() : "";
    }

    @Override
    public String toString() {
        return username;
    }
}