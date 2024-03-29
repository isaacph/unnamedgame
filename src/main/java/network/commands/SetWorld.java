package network.commands;

import game.ClientPayload;
import game.Game;
import model.TeamManager;
import model.World;
import server.ClientData;
import server.Server;
import network.ServerPayload;

public class SetWorld implements ClientPayload, ServerPayload {

    public World world;

    public SetWorld(World world) {
        this.world = world.deepCopy();
    }

    @Override
    public void execute(Game gameResources) {
        gameResources.world.setWorld(world);
        gameResources.clickBoxManager.resetGameObjectCache();
        gameResources.worldRenderer.resetGameObjectRenderCache();
        gameResources.animationManager.resetWhereNeeded();
        gameResources.worldRenderer.rebuildTerrain();
        gameResources.clickBoxManager.selectedID = null;
    }

    @Override
    public void execute(Server server, ClientData client) {
        TeamManager tm = server.world.teams;
        server.world.setWorld(world);
        server.world.teams = tm;
        server.broadcast(new ChatMessage("World set by " + client.name));
        server.broadcast(this, client);
    }
}
