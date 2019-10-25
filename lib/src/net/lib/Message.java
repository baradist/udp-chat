package net.lib;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class Message implements Serializable {

    private final ClientId sender;
    private final ClientId receiver;
    private final ZonedDateTime dateTime;
    private final String message;

    public Message(ClientId sender, ClientId receiver, ZonedDateTime dateTime, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.dateTime = dateTime;
        this.message = message;
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
