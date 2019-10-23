package net.lib;

import java.io.Serializable;
import java.util.Objects;

public final class ClientId implements Serializable {
    private final String login;

    public ClientId(String email) throws IllegalArgumentException {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid e-mail: " + email);
        }
        this.login = email.toLowerCase();
    }

    public String getLogin() {
        return login;
    }

    private boolean isValidEmail(String email) {
        return email != null && !email.isEmpty(); // TODO: do regex check
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientId clientId = (ClientId) o;
        return login.equals(clientId.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return "ClientId{" +
                "login='" + login + '\'' +
                '}';
    }
}
