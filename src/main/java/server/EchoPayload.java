package server;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;

public class EchoPayload implements ServerPayload {

    private String msg;

    public EchoPayload(String message) {
        this.msg = message;
    }

    @Override
    public void execute(Server server, int sourceCon) {
        if(this.msg == null) this.msg = "<empty>";
        server.toSend.add(new ClientPayloadID(gameResources -> {
            gameResources.chatbox.println(msg);
        }, sourceCon));
    }
}
