package server;

import game.ClientPayload;
import game.EmptyServerPayload;
import game.GameResources;
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
    public static final long AFK_TIME_MS = 60000;
    public static final long LIFE_CHECK_TIME_MS = 10000;

    private final Thread thread;
    private volatile boolean running;

    private int clientIdCounter = 0;

    public final Map<Integer, String> clientName = new HashMap<>();
    public ServerConnection<ServerPayload, ClientPayload> connection;

    public World world;

    public final Map<Integer, ClientData> clientIdMap = new HashMap<>();
    public final Map<Integer, Integer> connectionClientIdMap = new HashMap<>();
    public final Map<Integer, Integer> clientIdConnectionMap = new HashMap<>();

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

        long lastLifeCheck = System.currentTimeMillis();
        try {
            try {
                connection = new ServerConnection<>(PORT);
                connection.init();
                connection.setReadErrorHandler(incomingPairError -> {
                    System.err.println("Read error " + incomingPairError.clientID);
                    incomingPairError.e.printStackTrace();
                    return false;
                });
                connection.setConnectionRemoveHandler(connectionId -> {
                    int clientIdRemove = connectionClientIdMap.remove(connectionId);
                    clientIdConnectionMap.remove(clientIdRemove);
                    clientIdMap.remove(clientIdRemove);
                });
            } catch(IOException e) {
                System.err.println("Failed to init server connection");
                throw e;
            }
            world = new World();
            while(running) {
                Map<Integer, Collection<ServerPayload>> received = connection.pollAndSend();
                for(int conId : received.keySet()) {
                    ClientData client = loadClientFromConnection(conId);
                    for(ServerPayload payload : received.get(conId)) {
                        payload.execute(this, client);
                    }
                }

                // update world
                try {
//                    Thread.sleep(500);
                } catch (Exception e) {}

                long newTime = System.currentTimeMillis();
                if(newTime - lastLifeCheck > LIFE_CHECK_TIME_MS) {
//                    ArrayList<Integer> clientsToForget = new ArrayList<>();
//                    for(ClientData client : clientIdMap.values()) {
//                        if(newTime - client.lastMessage > AFK_TIME_MS) {
//                            clientsToForget.add(client.clientId);
//                        }
//                    }
//                    for(int clientIdRemove : clientsToForget) {
//                        clientIdMap.remove(clientIdRemove);
//                        int conId = clientIdConnectionMap.remove(clientIdRemove);
//                        connectionClientIdMap.remove(conId);
//                    }
                    broadcast((GameResources gameResources) ->
                            gameResources.connection.queueSend(new EmptyServerPayload()));
                    lastLifeCheck = newTime;
                }
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

    public void broadcast(Collection<ClientPayload> payload) {
        for(int clientId : clientIdMap.keySet()) {
            connection.send(getConnection(clientId), payload);
        }
    }

    private void broadcast(ClientPayload payload) {
        broadcast(Collections.singletonList(payload));
    }

    public ClientData loadClientFromConnection(int conId) {
        Integer clientId = connectionClientIdMap.get(conId);
        if(clientId == null || !clientIdMap.containsKey(clientId)) {
            clientId = clientIdCounter++;
            ClientData client = new ClientData(clientId);
            clientIdMap.put(clientId, client);
            clientIdConnectionMap.put(clientId, conId);
            connectionClientIdMap.put(conId, clientId);
        }
        ClientData client = clientIdMap.get(clientId);
        client.lastMessage = System.currentTimeMillis();
        return client;
    }

    public int getConnection(int clientId) {
        return clientIdConnectionMap.get(clientId);
    }
}
