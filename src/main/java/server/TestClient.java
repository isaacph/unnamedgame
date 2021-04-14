package server;

import game.ClientPayload;
import game.GameResources;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class TestClient implements Runnable {

    public void run() {
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress("localhost", Server.PORT));
            ServerPayload p = (server, sourceCon) -> server.toSend.add(new ClientPayloadID(
                (ClientPayload) gameResources -> System.out.println("test client payload"),
                sourceCon));
            while(!socketChannel.isConnectionPending()) {}
            socketChannel.finishConnect();
            try(ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(p);
                System.out.println(new String(bos.toByteArray(), StandardCharsets.UTF_8));
                socketChannel.write(ByteBuffer.wrap(bos.toByteArray()));
            }
            boolean recv = false;
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            int len;
            ClientPayload obj = null;
            while(!recv) {
                len = socketChannel.read(buffer);
                if(len > 0) {
                    recv = true;
                    ByteArrayInputStream bis = new ByteArrayInputStream(buffer.array(), 0, len);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    obj = (ClientPayload) ois.readObject();
                }
            }
            if(obj == null) {
                System.out.println("Received null client payload");
            } else {
                obj.execute(null);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(socketChannel != null && socketChannel.isOpen()) {
                try {
                    socketChannel.close();
                }
                catch (IOException e) {
                    System.err.println("Failed to close socket");
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String... args) {
        new Thread(new TestClient()).start();
    }
}
