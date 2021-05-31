package server.commands;

import game.TeamID;
import server.ClientData;
import server.Server;
import server.ServerPayload;

public class NextTurn implements ServerPayload {

    @Override
    public void execute(Server server, ClientData client) {
        TeamID currentTeam = server.world.teams.getTurn();
        if(currentTeam == null) {
            server.world.teams.startTurns();
            server.broadcast(new ChatMessage("Starting turns"));
            currentTeam = server.world.teams.getTurn();
            if(currentTeam == null) {
                if(client != null) {
                    server.send(client, new ChatMessage("Cannot find next turn without any teams"));
                }
                return;
            }
        } else {
            if(!server.world.teams.teamEndedTurn(currentTeam)) {
                if(client != null) {
                    server.send(client, new ChatMessage("Cannot end turn before all clients end turn"));
                }
                return;
            }
            server.world.teams.nextTurn();
            currentTeam = server.world.teams.getTurn();
        }
        server.world.teams.resetClientTurnEnd();
        server.world.resetGameObjectSpeeds();
        server.world.nextVersion();
        server.broadcast(new SetWorld(server.world));
        server.broadcast(new ChatMessage("New turn for team " + server.world.teams.getTeamName(currentTeam)));
    }
}
