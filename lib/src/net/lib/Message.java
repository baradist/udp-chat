package net.lib;

import net.lib.model.AddressPort;

import java.io.Serializable;
import java.net.InetAddress;
import java.time.ZonedDateTime;

public class Message implements Serializable {

    private final ClientId sender;
    private final ClientId receiver;
    private final ZonedDateTime dateTime;
    private final String message;
    private final AddressPort addressPort;

    public Message(ClientId sender, ClientId receiver, ZonedDateTime dateTime, InetAddress address, int port, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.dateTime = dateTime;
        this.message = message;
        addressPort = new AddressPort(address, port);
    }

    public ClientId getSender() {
        return sender;
    }

    public ClientId getReceiver() {
        return receiver;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public String getMessage() {
        return message;
    }

    public AddressPort getAddressPort() {
        return addressPort;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender=" + sender.toString() +
                ", receiver=" + receiver.toString() +
                ", dateTime=" + dateTime +
                ", message='" + message + '\'' +
                '}';
    }
}
