package server;

import game.ClientConnection;
import game.ClientPayload;
import game.GameResources;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

public class TestClient implements Runnable {

    ClientConnection<ClientPayload, ServerPayload> connection;

    public void run() {
        connection = new ClientConnection<>();
        connection.connect(new InetSocketAddress("localhost", Server.PORT));
        while(!connection.isConnected()) {
            connection.update();
        }
        int hc = this.hashCode();
        System.out.println(hc);
        connection.queueSend((server, clientData) -> {
            server.connection.send(server.getConnection(clientData.clientId),
                    Collections.singletonList(gameRes -> {
                System.out.println("test from client " + hc);
            }));
        });
        while(true) {
            Collection<ClientPayload> payloads = connection.update();
            for(ClientPayload p : payloads) {
                p.execute(null);
                break;
            }
        }
    }

    public static void main(String... args) {
        new TestClient().run();
    }
}
