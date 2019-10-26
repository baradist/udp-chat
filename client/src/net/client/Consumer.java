package net.client;

import net.lib.Message;
import net.lib.MessageDeserializer;
import util.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import static net.client.udp.UdpClient.BUFFER_SIZE;

public class Consumer implements Runnable {
    private static Logger logger = new Logger();

    private DatagramSocket input;
    private DatagramPacket packet;
    private byte[] buffer;
    private InetSocketAddress inetSocketAddress;
    private MessageDeserializer deserializer;

    public Consumer() throws SocketException {
        input = new DatagramSocket();
        buffer = new byte[BUFFER_SIZE];
        packet = new DatagramPacket(buffer, BUFFER_SIZE);
        inetSocketAddress = (InetSocketAddress) input.getLocalSocketAddress();
        deserializer = new MessageDeserializer();
    }

    @Override
    public void run() {
        logger.info("Client listens on " + input.getLocalSocketAddress().toString());
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

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    private void processMessage(Message msg) {
        printMessage(msg);
    }

    private void printMessage(Message msg) {
        logger.info(msg.toString());
    }
}
