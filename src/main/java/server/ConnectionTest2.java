package server;

import game.ClientConnection;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Collection;

import static server.ConnectionTest.Payload;

public class ConnectionTest2 {

    private static String makeBigStr(char c, int size) {
        StringBuilder buffer = new StringBuilder(size);
        for(int i = 0; i < size; ++i) {
            buffer.append(c);
        }
        return buffer.toString();
    }

    public static void main(String[] args) throws InterruptedException {
        int port = 1234;
        ClientConnection<Payload, Payload> client = new ClientConnection<>();
        client.connect(new InetSocketAddress("localhost", port));
        while(!client.isConnected()) client.update();
        client.queueSend(new Payload(makeBigStr('A', 1<<26)));
        while(client.isConnected()) {
            Collection<Payload> cp = client.update();
            for(Payload p : cp) {
                System.out.println("From server: ");
                Thread.sleep(200);
                client.queueSend(p);
            }
            Thread.sleep(200);
        }
    }
}
