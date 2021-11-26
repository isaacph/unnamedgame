package network.commands;

import game.ClientPayload;
import game.Game;
import model.ClientID;
import model.TeamID;
import server.ClientData;
import server.Server;
import network.ServerPayload;

public class EndTurn implements ServerPayload {

    public TeamID turnToEnd;

    public EndTurn(TeamID turnToEnd) {
        this.turnToEnd = turnToEnd;
    }

    @Override
    public void execute(Server server, ClientData client) {
        ClientID clientID = client.clientId;
        if(!server.world.teams.isClientsTurn(clientID)) {
            server.send(client, new ChatMessage("Client doesn't have turn"));
            return;
        }
        TeamID team = server.world.teams.getClientTeam(clientID);
        if(team == null) {
            server.send(client, new ChatMessage("Missing team"));
            return;
        }
        if(!team.equals(turnToEnd)) {
            server.send(client, new ChatMessage("Sent message with wrong teamID"));
            return;
        }
        server.world.teams.endClientTurn(clientID);
        if(server.world.teams.teamEndedTurn(team)) {
            new NextTurn().execute(server, client);
        }
    }
}
