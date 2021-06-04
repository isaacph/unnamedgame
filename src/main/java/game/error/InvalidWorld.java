package game.error;

import game.ClientPayload;
import game.Game;
import server.commands.GetWorld;

import java.util.UUID;

public class InvalidWorld implements ClientPayload {

    public UUID serverUUID;

    public InvalidWorld(UUID uuid) {
        this.serverUUID = uuid;
    }

    @Override
    public void execute(Game gameResources) {
        gameResources.chatbox.println("Invalid world: out of date");
        gameResources.chatbox.println("   client UUID: " + gameResources.world.getVersion());
        gameResources.chatbox.println("   server UUID: " + serverUUID);
        gameResources.chatbox.println("Resolving...");
        gameResources.connection.queueSend(new GetWorld());
    }
}
