package server;

import game.*;
import model.World;
import org.json.JSONObject;
import server.commands.ChatMessage;
import server.commands.ConnectionLifeCheck;
import model.ClientID;
import model.GameData;
import util.MathUtil;

import java.io.*;
import java.util.*;

public class Server {

    public static final int PORT = 6000;
    public static final long AFK_TIME_MS = 20000;
    public static final long LIFE_CHECK_TIME_MS = 5000;

    private final Thread thread;
    private volatile boolean running;

    public ServerConnection<ServerPayload, ClientPayload> connection;
    private final ClientID.Generator clientIDGenerator = new ClientID.Generator();

    public World world;
    public GameData gameData;

    public final Collection<ClientID> clientIDs = new ArrayList<>();
    public final Map<ClientID, ClientData> clientIdMap = new HashMap<>();
    public final Map<Integer, ClientID> connectionClientIdMap = new HashMap<>();
    public final Map<ClientID, Integer> clientIdConnectionMap = new HashMap<>();

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
                    System.err.println("Read error, client " + incomingPairError.clientID);
                    incomingPairError.e.printStackTrace();
                    return false;
                });
                connection.setConnectionRemoveHandler(connectionId -> {
                    ClientID clientIdRemove = connectionClientIdMap.remove(connectionId);
                    clientIdConnectionMap.remove(clientIdRemove);
                    clientIdMap.remove(clientIdRemove);
                    clientIDs.remove(clientIdRemove);
                });
            } catch(IOException e) {
                System.err.println("Failed to init server connection");
                throw e;
            }
            gameData = new GameData();
            try {
                String file = MathUtil.readFile("gamedata.json");
                this.gameData.fromJSON(new JSONObject(file), e -> {
                    System.err.println("Failed to parse JSON game data");
                    System.err.println(e.getMessage());
                });
            } catch(IOException e) {
                System.err.println("JSON file missing (probably)");
                e.printStackTrace();
            }
            world = new World();
            while(running) {
                long time = System.currentTimeMillis();

                Map<Integer, Collection<ServerPayload>> received = connection.pollAndSend();
                for(int conId : received.keySet()) {
                    ClientData client = loadClientFromConnection(conId);
                    client.lastMessage = time;
                    for(ServerPayload payload : received.get(conId)) {
                        payload.execute(this, client);
                    }
                }

                // update world
                try {
                    Thread.sleep(200);
                } catch (Exception e) {}

                if(time - lastLifeCheck > LIFE_CHECK_TIME_MS) {
                    lastLifeCheck = time;

                    Collection<ClientID> toRemove = new ArrayList<>();
                    for(ClientID clientID : clientIDs) {
                        long lastMessage = clientIdMap.get(clientID).lastMessage;
                        long diff = time - lastMessage;
                        if(diff > AFK_TIME_MS) {
                            toRemove.add(clientID);
                        }
                    }
                    for(ClientID id : toRemove) {
                        removeClient(id);
                    }

                    broadcast(new ConnectionLifeCheck());
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

    public void broadcast(Collection<ClientPayload> payload, Collection<ClientID> blacklist) {
        for(ClientID clientId : clientIDs) {
            if(!blacklist.contains(clientId)) {
                connection.send(getConnection(clientId), payload);
            }
        }
    }

    public void broadcast(ClientPayload payload, Collection<ClientID> blacklist) {
        broadcast(Collections.singletonList(payload), Collections.emptyList());
    }

    public void broadcast(ClientPayload payload, ClientID blacklist) {
        broadcast(Collections.singletonList(payload), Collections.singletonList(blacklist));
    }

    public void broadcast(ClientPayload payload, ClientData blacklist) {
        broadcast(Collections.singletonList(payload), Collections.singletonList(blacklist.clientId));
    }

    public void broadcast(Collection<ClientPayload> payload) {
        broadcast(payload, Collections.emptyList());
    }

    public void broadcast(ClientPayload payload) {
        broadcast(Collections.singletonList(payload));
    }

    public ClientData loadClientFromConnection(int conId) {
        ClientID clientId = connectionClientIdMap.get(conId);
        if(clientId == null || !clientIdMap.containsKey(clientId)) {
            clientId = clientIDGenerator.generate();
            clientIDs.add(clientId);
            ClientData client = new ClientData(clientId);
            client.lastMessage = System.currentTimeMillis();
            clientIdMap.put(clientId, client);
            clientIdConnectionMap.put(clientId, conId);
            connectionClientIdMap.put(conId, clientId);
        }
        ClientData client = clientIdMap.get(clientId);
        client.lastMessage = System.currentTimeMillis();
        return client;
    }

    public void removeClient(ClientID client) {
        boolean contained = clientIDs.remove(client);
        ClientData data = clientIdMap.remove(client);
        Integer conID = clientIdConnectionMap.remove(client);
        world.teams.setClientTeam(client, null);
        if(conID != null) {
            connectionClientIdMap.remove(conID);
            connection.cleanDisconnect(conID, new ChatMessage("Disconnecting..."));
        }
        if(contained && data != null) {
            broadcast(new ChatMessage(data.name + " has disconnected."));
        }
    }

    public int getConnection(ClientID clientId) {
        return clientIdConnectionMap.get(clientId);
    }

    public void send(ClientData client, ClientPayload payload) {
        if(client != null) {
            connection.send(getConnection(client.clientId), payload);
        }
    }
}
