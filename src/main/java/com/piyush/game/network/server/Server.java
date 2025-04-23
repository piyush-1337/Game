package com.piyush.game.network.server;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

public class Server {

    private Socket socket;
    private InetAddress ip;
    private String username;
    private final int gamePort;

    public Server(InetAddress ip, String username, int gamePort) {
        this.socket = null;
        this.ip = ip;
        this.username = username;
        this.gamePort = gamePort;
    }

    public int getGamePort() {
        return gamePort;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        if (socket != null) {
            this.ip = socket.getInetAddress();
        }
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
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
        if (!(o instanceof Server)) return false;
        Server server = (Server) o;
        return Objects.equals(username, server.username) &&
                Objects.equals(ip, server.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, ip);
    }

    @Override
    public String toString() {
        return username + " (" + (ip != null ? ip.getHostAddress() : "unknown") + ")";
    }
}
