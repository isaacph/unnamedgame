package network.commands;

import org.json.JSONException;
import org.json.JSONObject;
import server.ClientData;
import server.Server;
import network.ServerPayload;

public class SetWorldJSON implements ServerPayload {

    public String json;

    public SetWorldJSON(String src) {
        json = src;
    }

    @Override
    public void execute(Server server, ClientData client) {
        try {
            server.world.initFromJSON(new JSONObject(json), server.gameData);
            server.broadcast(new SetWorld(server.world));
            server.broadcast(new ChatMessage(client.name + " has loaded a world from a file"));
        } catch(JSONException e) {
            server.send(client, new ChatMessage("Failed to parse JSON file"));
            e.printStackTrace();
        } catch(Exception e) {
            server.send(client, new ChatMessage("Failed to load world"));
            e.printStackTrace();
        }
    }
}
