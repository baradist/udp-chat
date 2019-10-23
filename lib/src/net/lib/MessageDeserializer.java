package net.lib;

import util.Logger;

import java.io.*;

public class MessageDeserializer {

    private static Logger log = new Logger();

    public Message readMessage(byte[] data) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data))) {

            return (Message) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            log.error("Can not read DatagramPacket. " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public byte[] writeMessage(Message message) {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(message);

            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Can not write a message: " + message + ", " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
