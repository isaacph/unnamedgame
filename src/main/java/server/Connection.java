package server;

import server.ServerPayload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Connection<PayloadType, OutgoingType> {

    private SocketChannel socketChannel;
    private ByteBuffer buffer = ByteBuffer.allocate(8192);
    private ByteArrayOutputStream currentPacket = new ByteArrayOutputStream();
    private int bytesLeft = 0;

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

    public Collection<PayloadType> pollAndSend() {
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
        Collection<PayloadType> col = readFromSocket();

        return col;
    }

    private void sendToSocket(OutgoingType obj) {

    }

    private Collection<PayloadType> readFromSocket() {
        Collection<PayloadType> payloads = new ArrayList<>();
        try {
            int len, offset;
            buffer.clear();
            do {
                offset = 0;
                len = socketChannel.read(buffer);
                while (len - offset > 0) {
                    if(bytesLeft == 0) {
                        // read an int
                        ByteArrayInputStream bis = new ByteArrayInputStream(buffer.array(), offset, 4);
                        ObjectInputStream ois = new ObjectInputStream(bis);
                        bytesLeft = ois.readInt();
                        offset += 4;
                        if(bytesLeft <= 0 || bytesLeft > 10000) {
                            throw new RuntimeException("Received MASSIVE packet " + bytesLeft);
                        } else {
                            currentPacket.reset();
                        }
                    } else {
                        // read byteLeft text into currentPacket
                        currentPacket.write(buffer.array(), offset, Math.min(len - offset, bytesLeft));
                        bytesLeft -= Math.min(len - offset, bytesLeft);
                        offset += Math.min(len - offset, bytesLeft);
                        if(bytesLeft == 0) {
                            PayloadType p = convertFromBytes(currentPacket.toByteArray());
                            if(p != null) {
                                payloads.add(p);
                            }
                            currentPacket.reset();
                        }
                    }
                }
            } while(len > 0);
        } catch (IOException e) {
            System.err.println("Failed to read from socket");
        }
        return payloads;
    }

    @SuppressWarnings("unchecked")
    private PayloadType convertFromBytes(byte[] payload) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(currentPacket.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (PayloadType) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException | ClassNotFoundException e) {
            System.err.println("Received packet of bad type, size: " + payload.length);
            e.printStackTrace();
        }
        return null;
    }

    public void send(ServerPayload serverPayload) {

    }

    public boolean isConnected() {
        if(socketChannel == null) return false;
        return socketChannel.isConnected();
    }
}
