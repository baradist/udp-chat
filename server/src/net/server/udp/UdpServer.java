package net.server.udp;


import net.lib.ClientId;
import net.lib.Message;
import net.lib.MessageDeserializer;
import util.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpServer implements Runnable {
    public static final int SERVER_PORT = 12345;
    public static final int BUFFER_SIZE = 2048;
    private static Logger logger = new Logger();

    private Map<ClientId, Set<Message>> messageMap;
    private Map<ClientId, InetSocketAddress> lastAddressMap;
    private LinkedBlockingQueue<ClientId> messagesQueue;

    private MessageDeserializer deserializer;

    public UdpServer() {
        deserializer = new MessageDeserializer();
        messageMap = new ConcurrentHashMap<>();
        lastAddressMap = new ConcurrentHashMap<>();
        messagesQueue = new LinkedBlockingQueue<>();

//        ClientId receiver = new ClientId("1");
//        messageMap.put(receiver, new HashSet<Message>() {{
//            add(new Message(new ClientId("2"), receiver, null, ZonedDateTime.now(), "Message"));
//        }});
    }

    @Override
    public void run() {
        try {
            new Thread(new Consumer(SERVER_PORT)).start();
            new Thread(new Producer()).start();
            logger.info("UdpServer has been started");
        } catch (SocketException e) {
            logger.error("Can not connect to the server: " + e.getMessage());
        }
    }

    private void removeMessagesFor(ClientId receiver, Message message) {
        if (!messageMap.containsKey(receiver)) {
            return;
        }
        Set<Message> messages = messageMap.get(receiver);
        messages.remove(message);
        if (messages.isEmpty()) {
            messageMap.remove(receiver);
        }
    }

    private void removeMessagesFor(ClientId receiver) {
        messageMap.remove(receiver);
    }

    private Set<Message> getMessagesFor(ClientId receiver) {
        return messageMap.getOrDefault(receiver, Collections.emptySet());
    }

    private class Producer implements Runnable {
        private DatagramSocket socket;
        private DatagramPacket packet;
        private byte[] buffer;

        public Producer() throws SocketException {
            socket = new DatagramSocket();
            buffer = new byte[BUFFER_SIZE];
            packet = new DatagramPacket(buffer, buffer.length);
        }

        @Override
        public void run() {
            while (true) {
                ClientId receiver = null;
                try {
                    receiver = messagesQueue.take();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                    throw new RuntimeException(e);
                }
                if (!lastAddressMap.containsKey(receiver)) {
                    continue;
                }
                InetSocketAddress inetSocketAddress = lastAddressMap.get(receiver);
                Set<Message> messages = getMessagesFor(receiver);
                for (Message message : messages) {
                    sendMessage(inetSocketAddress, message);
                }
                removeMessagesFor(receiver);
            }
        }

        private void sendMessage(InetSocketAddress socketAddress, Message message) {
            logger.info("Send to " + socketAddress + ", Message: " + message);
            byte[] bytes = deserializer.writeMessage(message);
            packet.setData(bytes);
            packet.setLength(bytes.length);
            packet.setSocketAddress(socketAddress);

            try {
                socket.send(packet);
            } catch (IOException e) {
                logger.error("Can not send a packet: " + e.getMessage());
            }
        }
    }

    private class Consumer implements Runnable {
        private DatagramSocket input;
        private DatagramPacket packet;
        private byte[] buffer;
        private AtomicBoolean hasJob;

        public Consumer(int port) throws SocketException {
            input = new DatagramSocket(port);
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
                    processMessage(msg);
                } catch (IOException e) {
                    logger.error("Can not read DatagramPacket. " + e.getMessage());
                } finally {
//                    clearBuffer(); // TODO: ?
                }
            }
        }

        private void processMessage(Message message) {
            printMessage(message);
            ClientId receiver = message.getReceiver();

            lastAddressMap.put(message.getSender(),
                    new InetSocketAddress("localhost", message.getSenderInetSocketAddress().getPort())); // TODO

            addMessageToOutbox(message, receiver);
            resendExistingMessagesFor(message.getSender());
        }

        private void resendExistingMessagesFor(ClientId sender) {
            messagesQueue.add(sender);
        }

        private void addMessageToOutbox(Message msg, ClientId receiver) {
            if (messageMap.containsKey(receiver)) {
                messageMap.get(receiver).add(msg);
            } else {
                messageMap.put(receiver, new HashSet<Message>() {{
                    add(msg);
                }});
            }
            messagesQueue.add(receiver);
        }

        private void printMessage(Message msg) {
            logger.info("Received: " + msg.toString());
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
