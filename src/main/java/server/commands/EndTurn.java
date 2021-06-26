package server.commands;

import model.ClientID;
import model.TeamID;
import server.ClientData;
import server.Server;
import server.ServerPayload;

public class EndTurn implements ServerPayload {
    @Override
    public void execute(Server server, ClientData client) {
        ClientID clientID = client.clientId;
        if(!server.world.teams.isClientsTurn(clientID)) server.send(client, new ChatMessage("Client doesn't have turn"));
        TeamID team = server.world.teams.getClientTeam(clientID);
        if(team == null) server.send(client, new ChatMessage("Missing team"));
        server.world.teams.endClientTurn(clientID);
        if(server.world.teams.teamEndedTurn(team)) {
            new NextTurn().execute(server, client);
        }
    }
}
