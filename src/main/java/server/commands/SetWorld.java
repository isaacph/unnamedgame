package server.commands;

import game.ClientPayload;
import game.GameResources;
import game.TeamManager;
import game.World;
import server.ClientData;
import server.Server;
import server.ServerPayload;

import java.util.Collections;

public class SetWorld implements ClientPayload, ServerPayload {

    public World world;

    public SetWorld(World world) {
        this.world = world;
    }

    @Override
    public void execute(GameResources gameResources) {
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
