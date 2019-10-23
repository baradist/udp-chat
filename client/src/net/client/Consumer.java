package net.client;

import net.lib.Message;
import net.lib.MessageDeserializer;
import util.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import static net.client.udp.UdpClient.BUFFER_SIZE;

public class Consumer implements Runnable {
    private static Logger logger = new Logger();

    private DatagramSocket input;
    private DatagramPacket packet;
    private byte[] buffer;
    private MessageDeserializer deserializer;

    public Consumer(int port) throws SocketException {
        input = new DatagramSocket(port);
        buffer = new byte[BUFFER_SIZE];
        packet = new DatagramPacket(buffer, BUFFER_SIZE);
        deserializer = new MessageDeserializer();
    }

    @Override
    public void run() {
        while (true) {
            try {
                input.receive(packet);
                Message msg = deserializer.readMessage(packet.getData());
                processMessage(msg);
            } catch (IOException e) {
                logger.error("Can not read DatagramPacket. " + e.getMessage());
            } finally {
//                    clearBuffer(); // TODO: ?
            }
        }
    }

    private void processMessage(Message msg) {
        printMessage(msg);
    }

    private void printMessage(Message msg) {
        logger.info(msg.toString());
    }
}
