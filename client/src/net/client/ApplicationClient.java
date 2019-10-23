package net.client;

import net.client.udp.UdpClient;
import util.Logger;

public class ApplicationClient {
    private static Logger logger = new Logger();

    public static void main(String[] args) {
        try (UdpClient client = new UdpClient()) {
            logger.info("Start a client");
            client.doWork();
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }
}
