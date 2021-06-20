package game;

import server.ServerPayload;
import server.SocketChannelReader;
import server.SocketChannelWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class ClientConnection<IncomingType, OutgoingType> {

    private SocketChannel socketChannel;
    private SocketChannelReader<IncomingType> socketChannelReader = new SocketChannelReader<>();
    private SocketChannelWriter<OutgoingType> socketChannelWriter = new SocketChannelWriter<>();
    private Consumer<SocketAddress> onConnectHandler = socketAddress -> {};
    private SocketAddress localAddress;
    private SocketAddress remoteAddress;

    public void connect(SocketAddress remote) {
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
            this.localAddress = socketChannel.getLocalAddress();
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
            this.remoteAddress = remote;
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
                this.remoteAddress = socketChannel.getRemoteAddress();
                this.localAddress = socketChannel.getLocalAddress();
                this.onConnectHandler.accept(remoteAddress);
            } catch(IOException e) {
                System.err.println("Failed to finish connection");
                e.printStackTrace();
                return Collections.emptyList();
            }
        }
        if(!socketChannel.isConnected() || !socketChannel.isOpen()) return Collections.emptyList();
        try {
            socketChannelWriter.update();
        }catch(IOException e) {
            System.err.println("SocketChannelWriter update error");
            e.printStackTrace();
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
        socketChannelWriter.queueSend(socketChannel, payload);
    }

    public void clearQueue() {
        socketChannelWriter.clearQueue();
    }

    public boolean isConnected() {
        if(socketChannel == null) return false;
        return socketChannel.isConnected();
    }

    public SocketAddress getLocalAddress() {
        return localAddress;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setOnConnectHandler(Consumer<SocketAddress> handler) {
        this.onConnectHandler = handler;
    }
}
