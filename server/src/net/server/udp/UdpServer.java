package net.server.udp;


import net.lib.ClientId;
import net.lib.Message;
import net.lib.MessageDeserializer;
import net.lib.model.AddressPort;
import util.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpServer implements Runnable {
    public static final int SERVER_PORT = 12345;
    public static final int BUFFER_SIZE = 2048;
    private static Logger log = new Logger();

    private Map<ClientId, List<Message>> messageMap;
    private Map<ClientId, AddressPort> lastAddressMap;
    private Queue<ClientId> messagesQueue;

    private MessageDeserializer deserializer;

    public UdpServer() {
        deserializer = new MessageDeserializer();
        messageMap = new ConcurrentHashMap<>();
        lastAddressMap = new ConcurrentHashMap<>();
        messagesQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        try {
            new Thread(new Consumer()).start();
            log.info("UdpServer has been started");
        } catch (SocketException e) {
            log.error("Can not connect to the server: " + e.getMessage());
        }
    }

    private class Consumer implements Runnable {
        private DatagramSocket input;
        private DatagramPacket packet;
        private byte[] buffer;
        private AtomicBoolean hasJob;

        public Consumer() throws SocketException {
            input = new DatagramSocket(SERVER_PORT);
            buffer = new byte[BUFFER_SIZE];
            packet = new DatagramPacket(buffer, BUFFER_SIZE);
            hasJob = new AtomicBoolean(false);
        }

        @Override
        public void run() {
            setHasJob(true);
            while (hasJob.get()) {
                try {
                    input.receive(packet);
                    Message msg = deserializer.readMessage(packet.getData());
                    printMessage(msg);
                    ClientId receiver = msg.getReceiver();

                    lastAddressMap.put(msg.getSender(), msg.getAddressPort());

                    addMessageToOutbox(msg, receiver);
                    resendExistingMessagesFor(msg.getSender());
                } catch (IOException e) {
                    log.error("Can not read DatagramPacket. " + e.getMessage());
                } finally {
//                    clearBuffer(); // TODO: ?
                }
            }
        }

        private void resendExistingMessagesFor(ClientId sender) {
            messagesQueue.add(sender);
        }

        private void addMessageToOutbox(Message msg, ClientId receiver) {
            if (messageMap.containsKey(receiver)) {
                messageMap.get(receiver).add(msg);
            } else {
                messageMap.put(receiver, new ArrayList<Message>() {{
                    add(msg);
                }});
            }
            messagesQueue.add(receiver);
        }

        private void printMessage(Message msg) {
            log.info(msg.toString());
        }

        private void clearBuffer() {
            for (int i = 0; i < BUFFER_SIZE; i++) {
                buffer[i] = 0;
            }
        }

        public void setHasJob(boolean hasJob) {
            this.hasJob.set(hasJob);
        }
    }
}
