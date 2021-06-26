package network.commands;

import game.ClientPayload;
import game.Game;
import model.TeamID;
import server.ClientData;
import server.Server;
import network.ServerPayload;

import java.util.Collections;

public class ChatMessage implements ServerPayload, ClientPayload {

    public String message;

    public ChatMessage(String msg) {
        this.message = msg;
    }

    @Override
    public void execute(Server server, ClientData client) {
        TeamID team = server.world.teams.getClientTeam(client.clientId);
        String teamPrefix = "";
        if(team != null) {
            teamPrefix = "<" +
                    server.world.teams.getTeamName(team)
                    + "> ";
        }
        server.broadcast(Collections.singletonList(
                new ChatMessage(teamPrefix + server.clientIdMap.get(client.clientId).name + ": " + message)
        ));
    }

    @Override
    public void execute(Game gameResources) {
        gameResources.chatbox.println(message);
    }
}
