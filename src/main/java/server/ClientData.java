package server;

import game.ClientID;

public class ClientData {
    public String name;
    public ClientID clientId;
    public long lastMessage;

    public ClientData(ClientID clientId) {
        this.clientId = clientId;
        this.name = "Unnamed" + clientId.hashCode();
    }
}
