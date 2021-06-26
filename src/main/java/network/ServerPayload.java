package network;

import server.ClientData;
import server.Server;

import java.io.Serializable;

public interface ServerPayload extends Serializable {

    void execute(Server server, ClientData client);
}
