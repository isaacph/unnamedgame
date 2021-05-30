package server.commands;

import game.ClientPayload;
import game.EmptyServerPayload;
import game.GameResources;

public class ConnectionLifeCheck implements ClientPayload {
    @Override
    public void execute(GameResources gameResources) {
        gameResources.connection.queueSend(new EmptyServerPayload());
    }
}
