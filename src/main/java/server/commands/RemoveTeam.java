package server.commands;

import model.TeamID;
import server.ClientData;
import server.Server;
import server.ServerPayload;

public class RemoveTeam implements ServerPayload {

    private final TeamID teamID;

    public RemoveTeam(TeamID team) {
        this.teamID = team;
    }

    @Override
    public void execute(Server server, ClientData client) {
        if(teamID == null) server.send(client, new ChatMessage("Team not found"));
        String teamName = server.world.teams.getTeamName(teamID);
        boolean rem = server.world.teams.removeTeam(teamID);
        if(rem) {
            server.broadcast(new ChatMessage("Team " + teamName + " deleted."));
            server.broadcast(new SetTeams(server.world.teams));
        }
        else server.send(client, new ChatMessage("Team not found"));
    }
}
