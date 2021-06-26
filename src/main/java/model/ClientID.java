package model;

import java.io.Serializable;

public class ClientID implements Serializable {

    private final int id;

    private ClientID(int id) {
        this.id = id;
    }

    public ClientID(ClientID other) {
        this.id = other.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ClientID && ((ClientID) other).id == id;
    }

    @Override
    public String toString() {
        return "ClientID#" + id;
    }

    public static class Generator {

        private int generatorNumber;

        public Generator() {
            this.generatorNumber = 0;
        }

        public ClientID generate() {
            return new ClientID(++generatorNumber);
        }
    }

    public static ClientID getPlaceholder() {
        return new ClientID(-1);
    }
}
