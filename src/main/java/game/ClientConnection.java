package game;

import server.ServerPayload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Collections;

public class ClientConnection {

    private SocketChannel socketChannel;
    private ByteBuffer buffer = ByteBuffer.allocate(8192);

    public void connect(SocketAddress remote) {
        if(socketChannel != null) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                System.err.println("Failed to close connection");
                e.printStackTrace();
                socketChannel = null;
            }
        }
        if(socketChannel == null) {
            try {
                socketChannel = SocketChannel.open();
            } catch (IOException e) {
                System.err.println("Failed to open socket");
                e.printStackTrace();
                try {
                    socketChannel.close();
                } catch (Exception ignored) {}
                return;
            }
        }
        if(remote == null) {
            throw new RuntimeException("Tried to connect to null address");
        }
        try {
            socketChannel.connect(remote);
        } catch (IOException e) {
            System.err.println("Failed to connect to remote " + remote.toString());
            e.printStackTrace();
            return;
        }
    }

    public Collection<ClientPayload> pollAndSend() {
        if(socketChannel == null) {
            return Collections.emptyList();
        }
        if(socketChannel.isConnectionPending()) {
            try {
                socketChannel.finishConnect();
            } catch(IOException e) {
                System.err.println("Failed to finish connection");
                e.printStackTrace();
                return Collections.emptyList();
            }
        }
        if(!socketChannel.isConnected()) return Collections.emptyList();
        try {
            int len = socketChannel.read(buffer);
            if (len > 0) {
                ByteArrayInputStream bis = new ByteArrayInputStream(buffer.array(), 0, len);
                ObjectInputStream ois = new ObjectInputStream(bis);
                obj = (ClientPayload) ois.readObject();
            }
        } catch (IOException e) {
            System.err.println("Failed to read from socket");
        }
    }

    public void send(ServerPayload serverPayload) {

    }

    public boolean isConnected() {
        if(socketChannel == null) return false;
        return socketChannel.isConnected();
    }
}
