package server.commands;

import server.ClientData;
import server.Server;
import server.ServerPayload;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.util.Collections;

public class EchoPayload implements ServerPayload {

    private String msg;

    public EchoPayload(String message) {
        this.msg = message;
    }

    @Override
    public void execute(Server server, ClientData client) {
        if(this.msg == null) this.msg = "<empty>";
        server.connection.send(server.getConnection(client.clientId),
                Collections.singletonList(gameResources ->
                    gameResources.chatbox.println(msg)));
    }
}
