package net.lib.model;

import java.io.Serializable;
import java.net.InetAddress;

public class AddressPort implements Serializable {
    private final InetAddress address;
    private final int port;

    public AddressPort(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }
}
