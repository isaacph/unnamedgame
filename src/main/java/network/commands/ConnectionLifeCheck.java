package network.commands;

import game.ClientPayload;
import game.EmptyServerPayload;
import game.Game;

public class ConnectionLifeCheck implements ClientPayload {
    @Override
    public void execute(Game gameResources) {
        gameResources.connection.queueSend(new EmptyServerPayload());
    }
}
