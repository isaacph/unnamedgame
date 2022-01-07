package server;

import game.ClientPayload;

import java.net.InetAddress;

public class ClientPayloadID {
    public ClientPayload payload;
    public int connectionID;

    public ClientPayloadID(ClientPayload p, int cid) {
        this.payload = p;
        this.connectionID = cid;
    }
}
