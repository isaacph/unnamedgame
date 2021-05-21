package server;

import game.ClientPayload;
import game.GameResources;

import java.util.Collections;

public class ChatMessage implements ServerPayload {

    public String message;

    public ChatMessage(String msg) {
        this.message = msg;
    }

    @Override
    public void execute(Server server, ClientData client) {
        server.broadcast(Collections.singletonList(
                new ChatMessage.Client("<" + server.clientName.get(client.clientId) + "> " + message)
        ));
    }

    private static class Client implements ClientPayload {
        String message;
        Client(String message) {
            this.message = message;
        }

        @Override
        public void execute(GameResources gameResources) {
            gameResources.chatbox.println(message);
        }
    }
}
