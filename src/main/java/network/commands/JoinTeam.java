package network.commands;

import model.TeamID;
import server.ClientData;
import server.Server;
import network.ServerPayload;

public class JoinTeam implements ServerPayload {

    private String targetTeam;

    public JoinTeam(String targetTeam) {
        this.targetTeam = targetTeam;
    }

    @Override
    public void execute(Server server, ClientData client) {
        if(targetTeam == null || targetTeam.length() <= 1) return;

        TeamID teamID = server.world.teams.getTeamWithName(targetTeam);
        if(teamID == null) {
            teamID = server.world.teams.teamIDGenerator.generate();
            server.world.teams.addTeam(teamID);
            server.world.teams.setTeamName(teamID, targetTeam);
        }
        server.world.teams.setClientTeam(client.clientId, teamID);
        server.send(client, new SetTeams(server.world.teams));
        server.broadcast(new ChatMessage("Player " + client.name + " has joined team " + targetTeam));
        if(server.world.teams.getTurn() != null && server.world.teams.getTurn().equals(teamID)) {
            server.send(client, new ChatMessage("Your turn!"));
        }
    }
}
