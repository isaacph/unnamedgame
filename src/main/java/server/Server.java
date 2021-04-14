package server;

import game.ClientPayload;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Server {

    public static final int PORT = 6000;

    private final Thread thread;
    private volatile boolean running = false;

    private ServerSocketChannel serverSocketChannel;
    private final ByteBuffer buffer = ByteBuffer.allocate(8192);
    public final Collection<ClientPayloadID> toSend = new ArrayList<>();

    private final Map<Integer, SocketChannel> connections = new HashMap<>();
    private int connectionCounter = 0;

    public Server() {
        thread = new Thread(this::run);
        running = true;
    }

    public void start() {
        if(thread.isAlive())
            throw new RuntimeException("Attempted to start server that's running!");
        thread.start();
    }

    private Collection<ServerPayload> readFromSocketChannel(int conId) throws IOException {
        SocketChannel socket = connections.get(conId);
        if(socket == null) {
            return null;
        }
        buffer.clear();
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            int length, total = 0;
            do {
                length = socket.read(buffer);
                if(length > 0) {
                    total += length;
                    bos.write(buffer.array(), 0, length);
                }
                buffer.clear();
            } while (length > 0);
            if(total > 0) {
                System.out.println(total);
                try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    Collection<ServerPayload> payloads = new ArrayList<>();
                    payloads.add((ServerPayload) ois.readObject());
                    return payloads;
                }
                catch (ClassNotFoundException e) {
                    System.err.println("ServerPayload: invalid type");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return Collections.emptyList();
    }

    private void writeToSocketChannel(int conId, Collection<ClientPayload> payload) throws IOException {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            for(ClientPayload p : payload) {
                oos.writeObject(p);
            }
            System.out.println("W: " + bos.toByteArray().length);
            connections.get(conId).write(ByteBuffer.wrap(bos.toByteArray()));
        }
    }

    private void run() {
        if(!running) {
            throw new RuntimeException("Attempted to run server that's already stopped");
        }

        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            while(running) {

                // get new connections
                SocketChannel socketChannel = serverSocketChannel.accept();
                while(socketChannel != null) {
                    socketChannel.configureBlocking(false);
                    connections.put(makeUniqueConnectionID(), socketChannel);
                    socketChannel = serverSocketChannel.accept();
                }

                // read from existing connections
                Collection<Integer> conToRemove = new ArrayList<>();
                for(int conId : connections.keySet()) {
                    Collection<ServerPayload> payloads = readFromSocketChannel(conId);
                    if(payloads != null) {
                        for (ServerPayload payload : payloads) {
                            payload.execute(this, conId);
                        }
                    } else {
                        conToRemove.add(conId);
                    }
                }
                for(int conId : conToRemove) {
                    connections.remove(conId);
                }

                // update world
                try {
                    Thread.sleep(500);
                } catch (Exception e) {}

                // send all messages in queue
                for(ClientPayloadID cpi : toSend) {
                    ClientPayload cp = cpi.payload;
                    writeToSocketChannel(cpi.connectionID, Collections.singletonList(cp));
                }
                toSend.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Server stopping due to errors.");
            running = false;
        } finally {
            System.out.println("Read thread ending");
        }
    }

    private int makeUniqueConnectionID() {
        return connectionCounter++;
    }

    public void stop() {
        running = false;
    }
}
