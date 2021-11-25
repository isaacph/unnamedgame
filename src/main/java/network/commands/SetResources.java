package network.commands;

import game.ClientPayload;
import game.Game;
import model.Action;
import model.ResourceID;
import model.TeamID;
import model.World;
import network.ServerPayload;
import server.ClientData;
import server.Server;

public class SetResources implements ServerPayload, ClientPayload {

    private TeamID teamID;
    private ResourceID resourceID;
    private int amount;

    public SetResources(TeamID teamID, ResourceID resourceID, int amount) {
        this.teamID = teamID;
        this.resourceID = resourceID;
        this.amount = amount;
    }

    @Override
    public void execute(Server server, ClientData client) {
        if(!server.world.teams.isTeam(teamID) || server.gameData.getResourceType(resourceID) == null || amount < 0) {
            server.send(client, gameResources -> gameResources.chatbox.println("Server read command as invalid."));
            server.send(client, new SetWorld(server.world));
            return;
        }

        server.world.teams.setTeamResource(teamID, resourceID, amount);
        server.broadcast(this, client);
    }

    @Override
    public void execute(Game gameResources) {
        gameResources.world.teams.setTeamResource(teamID, resourceID, amount);
    }
}
