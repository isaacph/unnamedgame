package server.commands;

import game.Action;
import game.ClientPayload;
import game.GameResources;
import game.World;
import game.error.InvalidWorld;
import server.ClientData;
import server.Server;
import server.ServerPayload;

import java.util.UUID;

public class ActionCommand implements ServerPayload, ClientPayload {

    public Action action;
    public UUID originalWorldVersion;

    public ActionCommand(Action action, World originWorld) {
        this.action = action;
        this.originalWorldVersion = originWorld.getVersion();
    }

    @Override
    public void execute(GameResources gameResources) {
        action.animate(gameResources);
    }

    @Override
    public void execute(Server server, ClientData client) {
        if(!originalWorldVersion.equals(server.world.getVersion())) {
            server.send(client, new InvalidWorld(server.world.getVersion()));
            return;
        }

        if(action.validate(client.clientId, server.world, server.gameData)) {
            action.execute(server.world, server.gameData);
            server.broadcast(this, client);
        } else {
            server.send(client, new ChatMessage("Invalid action"));
            server.send(client, new SetWorld(server.world));
        }
    }
}
