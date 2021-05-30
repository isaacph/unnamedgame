package server.commands;

import server.ClientData;
import server.Server;
import server.ServerPayload;

public class GetWorld implements ServerPayload {
    @Override
    public void execute(Server server, ClientData client) {
        server.send(client, new SetWorld(server.world));
    }
}
