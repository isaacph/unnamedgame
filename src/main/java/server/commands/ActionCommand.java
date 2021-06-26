package server.commands;

import model.Action;
import game.ClientPayload;
import game.Game;
import model.World;
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
    public void execute(Game gameResources) {
        gameResources.runAction(action);
    }

    @Override
    public void execute(Server server, ClientData client) {
        if(!originalWorldVersion.equals(server.world.getVersion())) {
            server.send(client, new InvalidWorld(server.world.getVersion()));
            return;
        }
        if(!server.world.teams.isClientsTurn(client.clientId)) {
            server.send(client, new ChatMessage("Not your turn"));
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
