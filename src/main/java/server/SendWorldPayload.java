package server;

import game.ClientPayload;
import game.GameResources;
import game.World;

public class SendWorldPayload implements ClientPayload {

    private World world;
    public SendWorldPayload(World world) {
        this.world = world;
    }

    @Override
    public void execute(GameResources gameResources) {
        gameResources.world.setWorld(world);
        gameResources.animationManager.reset();
        gameResources.clickBoxManager.resetGameObjectCache();
        gameResources.worldRenderer.resetGameObjectRenderCache();
        gameResources.worldRenderer.rebuildTerrain();
    }
}
