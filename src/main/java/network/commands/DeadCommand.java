package network.commands;

import model.GameObject;
import server.ClientData;
import server.Server;
import network.ServerPayload;

import java.util.ArrayList;
import java.util.List;

public class DeadCommand implements ServerPayload {
    @Override
    public void execute(Server server, ClientData client) {
        List<GameObject> toRemove = new ArrayList<>();
        for(GameObject object : server.world.gameObjects.values()) {
            if(!object.alive) {
                toRemove.add(object);
            }
        }
        for(GameObject object : toRemove) {
            server.world.gameObjects.remove(object.uniqueID);
        }
        server.broadcast(new SetWorld(server.world));
        server.broadcast(new ChatMessage("Dead cleared."));
    }
}
