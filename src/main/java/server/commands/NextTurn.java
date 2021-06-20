package server.commands;

import game.TeamID;
import game.World;
import server.ClientData;
import server.Server;
import server.ServerPayload;

import java.util.function.Consumer;

public class NextTurn implements ServerPayload {

    @Override
    public void execute(Server server, ClientData client) {
        executeNextTurn(server.world,
                s -> server.broadcast(new ChatMessage(s)),
                s -> server.send(client, new ChatMessage(s)));
        server.broadcast(new SetWorld(server.world));
    }

    public static void executeNextTurn(World world, Consumer<String> outputMessage, Consumer<String> outputError) {
        TeamID currentTeam = world.teams.getTurn();
        if(currentTeam == null) {
            world.teams.startTurns();
            outputMessage.accept("Starting turns");
            currentTeam = world.teams.getTurn();
            if(currentTeam == null) {
                outputError.accept("Cannot find next turn without any teams");
                return;
            }
        } else {
            if(!world.teams.teamEndedTurn(currentTeam)) {
                outputError.accept("Cannot end turn before all clients end turn");
                return;
            }
            world.teams.nextTurn();
            currentTeam = world.teams.getTurn();
        }
        world.teams.resetClientTurnEnd();
        world.resetGameObjectSpeeds();
        world.nextVersion();
        outputMessage.accept("New turn for team " + world.teams.getTeamName(currentTeam));
    }
}
