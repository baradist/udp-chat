package net.client.udp;

import net.client.Consumer;
import net.lib.ClientId;
import net.lib.Message;
import net.lib.MessageDeserializer;
import util.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.time.ZonedDateTime;
import java.util.Scanner;

public class UdpClient implements Closeable {
    public static final int SERVER_PORT = 12345;
    public static final int BUFFER_SIZE = 2048; // TODO: make a single common variable
    public static final String SERVER_HOST = "localhost";
    private static Logger logger = new Logger();
    private final Consumer consumer;
    private ClientId clientId;
    private Message message;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] buffer;
    private Scanner scanner;
    private InetAddress server;
    private MessageDeserializer messageDeserializer;
    private String host;
    private int port;

    public UdpClient() throws UnknownHostException, SocketException {
        scanner = new Scanner(System.in);
        server = InetAddress.getByName(SERVER_HOST);
        socket = new DatagramSocket();
        buffer = new byte[BUFFER_SIZE];
        packet = new DatagramPacket(buffer, buffer.length, server, SERVER_PORT);
        consumer = new Consumer();
        messageDeserializer = new MessageDeserializer();
    }

    public void doWork() throws UnknownHostException, SocketException {

        new Thread(consumer).start();

        String login = askForInput("Enter login");
        while (!isValidLogin(login)) {
            System.out.println("Invalid login: " + login);
            return;
        }
        ClientId sender = new ClientId(login);

        while (true) {
            String receiverLogin = askForInput("Enter receiver: ");
            ClientId receiver = new ClientId(receiverLogin);
            String text = askForInput("Enter a message: ");
            Message message = new Message(sender, receiver, consumer.getInetSocketAddress(), ZonedDateTime.now(), text);
            sendMessage(message);
        }
    }

    private void sendMessage(Message message) {
        byte[] bytes = messageDeserializer.writeMessage(message);
        packet.setData(bytes);
        packet.setLength(bytes.length);
        try {
            socket.send(packet);
        } catch (IOException e) {
            logger.error("Can not send a packet: " + e.getMessage());
        }
    }

    private boolean isValidLogin(String login) {
        return login != null && !login.trim().isEmpty();
    }

    private String askForInput(String invite) {
        System.out.println(invite + " > ");
        return scanner.nextLine();

    }

    @Override
    public void close() throws IOException {

    }
}
