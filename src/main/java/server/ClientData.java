package server;

public class ClientData {
    public String name;
    public int clientId;
    public long lastMessage;

    public ClientData(int clientId) {
        this.clientId = clientId;
        this.name = "Unnamed";
    }
}
