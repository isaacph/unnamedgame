package game;

import server.ServerPayload;
import server.SocketChannelReader;
import server.SocketChannelWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ClientConnection<IncomingType, OutgoingType> {

    private SocketChannel socketChannel;
    private SocketChannelReader<IncomingType> socketChannelReader = new SocketChannelReader<>();
    private SocketChannelWriter<OutgoingType> socketChannelWriter = new SocketChannelWriter<>();
    private ArrayList<OutgoingType> toSend = new ArrayList<>();

    public void connect(SocketAddress remote) {
        toSend.clear();
        if(socketChannel != null) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                System.err.println("Failed to close connection");
                e.printStackTrace();
            }
            socketChannel = null;
        }
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            System.err.println("Failed to open socket");
            e.printStackTrace();
            try {
                socketChannel.close();
            } catch (Exception ignored) {}
            socketChannel = null;
            return;
        }
        if(remote == null) {
            throw new RuntimeException("Tried to connect to null address");
        }
        try {
            socketChannel.connect(remote);
        } catch (IOException e) {
            System.err.println("Failed to connect to remote " + remote.toString());
            e.printStackTrace();
            socketChannel = null;
        } catch(UnresolvedAddressException e) {
            System.err.println("Unknown remote address " + remote.toString());
            e.printStackTrace();
            socketChannel = null;
        }
    }

    public Collection<IncomingType> update() {
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
            if(!toSend.isEmpty()) {
                socketChannelWriter.writeTo(socketChannel, toSend);
            }
        } catch(IOException e) {
            System.err.println("Failed to write to socket");
            e.printStackTrace();
        } finally {
            toSend.clear();
        }

        try {
            return socketChannelReader.readFrom(socketChannel);
        } catch (IOException e) {
            System.err.println("Failed to read from socket, closing");
            e.printStackTrace();
            this.close();
        }
        return Collections.emptyList();
    }

    public void close() {
        try {
            socketChannel.close();
        } catch(IOException e) {
            System.err.println("Socket close error");
            e.printStackTrace();
        } finally {
            socketChannel = null;
        }
    }

    public void queueSend(OutgoingType payload) {
        toSend.add(payload);
    }

    public boolean isConnected() {
        if(socketChannel == null) return false;
        return socketChannel.isConnected();
    }
}
