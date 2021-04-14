package server;

import java.io.Serializable;
import java.nio.channels.SocketChannel;

public interface ServerPayload extends Serializable {

    void execute(Server server, int sourceCon);
}
