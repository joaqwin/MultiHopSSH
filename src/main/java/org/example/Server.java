package org.example;

public class Server {
    String ip, user, password, name;
    int port;
    public Server(String ip, String user, String password, int port, String name){
        this.ip = ip;
        this.user = user;
        this.password = password;
        this.port = port;
        this.name = name;
    }
}
