package server.commands;

import game.ClientPayload;
import game.GameResources;
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
        gameResources.animationManager.reset();
        gameResources.clickBoxManager.resetGameObjectCache();
        gameResources.worldRenderer.resetGameObjectRenderCache();
        gameResources.worldRenderer.rebuildTerrain();
        gameResources.clickBoxManager.selectedID = null;
    }

    @Override
    public void execute(Server server, ClientData client) {
        server.world.setWorld(world);
        server.broadcast(new ChatMessage("World set by " + client.name));
        server.broadcast(this, client);
    }
}
