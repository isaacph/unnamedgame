package game;

import server.ClientData;
import server.Server;
import server.ServerPayload;

public class EmptyServerPayload implements ServerPayload {
    @Override
    public void execute(Server server, ClientData client) {

    }
}
