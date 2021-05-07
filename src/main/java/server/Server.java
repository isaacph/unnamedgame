package server;

import game.ClientPayload;
import game.World;

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
    private volatile boolean running;

    public static class ClientData {
        public String name;
        public int conId;
    }

    private final Map<Integer, ClientData> clientData = new HashMap<>();
    public ServerConnection<ServerPayload, ClientPayload> connection;

    public World world;

    public Server() {
        thread = new Thread(this::run);
        running = true;
    }

    public void start() {
        if(thread.isAlive())
            throw new RuntimeException("Attempted to start server that's running!");
        thread.start();
    }

    private void run() {
        if(!running) {
            throw new RuntimeException("Attempted to run server that's already stopped");
        }

        try {
            try {
                connection = new ServerConnection<>(PORT);
                connection.init();
            } catch(IOException e) {
                System.err.println("Failed to init server connection");
                throw e;
            }
            world = new World();
            while(running) {
                Map<Integer, Collection<ServerPayload>> received = connection.pollAndSend();
                for(int clientId : received.keySet()) {
                    for(ServerPayload payload : received.get(clientId)) {
                        payload.execute(this, clientId);
                    }
                }

                // update world
                try {
//                    Thread.sleep(500);
                } catch (Exception e) {}
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Server stopping due to errors.");
            running = false;
        } finally {
            System.out.println("Read thread ending");
        }
    }

    public void stop() {
        running = false;
    }
}
