package net.server;


import net.server.udp.UdpServer;

public class ApplicationServer {
    public static void main(String[] args) {
        new Thread(new UdpServer()).start();
    }
}
