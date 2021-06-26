package server.commands;

import model.TeamID;
import org.joml.Vector3f;
import server.ClientData;
import server.Server;
import server.ServerPayload;

public class SetTeamColor implements ServerPayload {

    private String teamName;
    private Vector3f color;

    public SetTeamColor(String teamName, Vector3f color) {
        this.teamName = teamName;
        this.color = color;
    }

    @Override
    public void execute(Server server, ClientData client) {
        if(teamName == null) server.send(client, new ChatMessage("Bad team name"));
        if(color == null) server.send(client, new ChatMessage("Bad color"));
        TeamID team = server.world.teams.getTeamWithName(teamName);
        String name = server.world.teams.getTeamName(team);
        if(team == null || name == null) server.send(client, new ChatMessage("Team not found"));
        server.world.teams.setTeamColor(team, color);
        server.broadcast(new ChatMessage("Team " + name + " changed color"));
        server.broadcast(new SetTeams(server.world.teams));
    }
}
