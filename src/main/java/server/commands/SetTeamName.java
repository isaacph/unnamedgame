package server.commands;

import model.TeamID;
import server.ClientData;
import server.Server;
import server.ServerPayload;

public class SetTeamName implements ServerPayload {

    public String desiredName;

    public SetTeamName(String desiredName) {
        this.desiredName = desiredName;
    }

    @Override
    public void execute(Server server, ClientData client) {
        String formatted = desiredName.trim();
        TeamID team = server.world.teams.getClientTeam(client.clientId);
        if(team == null) {
            server.send(client, new ChatMessage("You don't have a team"));
            return;
        }
        String prevName = server.world.teams.getTeamName(team);
        if(prevName.equals(formatted)) {
            server.send(client, new ChatMessage("Your team already had that name"));
            return;
        }
        server.world.teams.setTeamName(team, formatted);
        server.broadcast(new ChatMessage("Team " + prevName + " has changed to " + formatted));
    }
}
