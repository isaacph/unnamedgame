package server.commands;

import game.ClientPayload;
import game.Game;
import org.json.JSONObject;
import server.ClientData;
import server.Server;
import server.ServerPayload;
import model.GameData;

public class SetGameData implements ServerPayload {

    public String gameDataString;

    public SetGameData(GameData source) {
        gameDataString = source.toJSON().toString();
    }

    @Override
    public void execute(Server server, ClientData client) {
        if(server.gameData.fromJSON(new JSONObject(gameDataString), exception -> {
            System.err.println("Failed to parse new game data from client " + client.toString());
            exception.printStackTrace();
        })) {
            server.broadcast(new SetGameData.Client(server.gameData));
        }
    }

    public static class Client implements ClientPayload {

        public String gameDataString;

        public Client(GameData source) {
            gameDataString = source.toJSON().toString();
        }

        @Override
        public void execute(Game game) {
            game.gameData.fromJSON(new JSONObject(gameDataString), e -> {
                game.chatbox.println("Failed to parse server's game data");
                game.chatbox.println(e.getMessage());
                e.printStackTrace();
            });
        }
    }
}
