package server.commands;

import game.ClientID;
import game.ClientPayload;
import game.Game;
import server.ClientData;
import server.Server;
import server.ServerPayload;

public class GetClientID implements ServerPayload {

    @Override
    public void execute(Server server, ClientData client) {
        server.send(client, new IDReceived(client.clientId));
    }

    private static class IDReceived implements ClientPayload {
        private ClientID clientID;

        public IDReceived(ClientID id) {
            this.clientID = id;
        }

        @Override
        public void execute(Game gameResources) {
            gameResources.clientInfo.clientID = clientID;
        }
    }
}
