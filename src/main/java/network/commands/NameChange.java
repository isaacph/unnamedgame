package network.commands;

import model.ClientID;
import server.ClientData;
import server.Server;
import network.ServerPayload;

public class NameChange implements ServerPayload {

    public String desiredName;

    public NameChange(String name) {
        this.desiredName = name;
    }

    @Override
    public void execute(Server server, ClientData client) {
        boolean invalid = desiredName == null || desiredName.length() < 3 || desiredName.contains(" ") || desiredName.startsWith("ClientID") || desiredName.startsWith("Unnamed");
        for(ClientID id : server.clientIDs) {
            if(server.clientIdMap.get(id).name.equalsIgnoreCase(desiredName)) {
                invalid = true;
                break;
            }
        }
        if(invalid) {
            server.send(client, new ChatMessage("Invalid name change: " + desiredName));
            return;
        }
        String oldName = server.clientIdMap.get(client.clientId).name;
        server.clientIdMap.get(client.clientId).name = desiredName;
        server.broadcast(new ChatMessage("Name change: " + oldName + " -> " + desiredName));
    }
}
