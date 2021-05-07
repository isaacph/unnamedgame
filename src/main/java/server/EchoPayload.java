package server;

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
    public void execute(Server server, int sourceCon) {
        if(this.msg == null) this.msg = "<empty>";
        server.connection.send(sourceCon,
                Collections.singletonList(gameResources ->
                    gameResources.chatbox.println(msg)));
    }
}
