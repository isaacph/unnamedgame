package network.commands;

import model.ClientID;
import model.TeamID;
import server.ClientData;
import server.Server;
import network.ServerPayload;

import java.util.Collection;

public class ListTeams implements ServerPayload {
    @Override
    public void execute(Server server, ClientData client) {
        StringBuilder output = new StringBuilder();
        Collection<TeamID> teams = server.world.teams.getTeams();
        output.append("Teams:\n");
        for(TeamID team : teams) {
            output.append(server.world.teams.getTeamName(team)).append(": ");
            for(ClientID c : server.world.teams.getTeamClients(team)) {
                if(server.clientIdMap.get(c) != null) {
                    output.append(server.clientIdMap.get(c).name).append(", ");
                }
            }
            output.delete(output.length() - 2, output.length());
            output.append("\n");
        }
        output.deleteCharAt(output.length() - 1);
        server.send(client, new ChatMessage(output.toString()));
    }
}
