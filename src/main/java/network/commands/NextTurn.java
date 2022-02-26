package network.commands;

import game.AbilityOrganizer;
import game.ClientPayload;
import game.Game;
import model.*;
import model.abilities.AbilityComponent;
import server.ClientData;
import server.Server;
import network.ServerPayload;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class NextTurn implements ServerPayload, ClientPayload {

    @Override
    public void execute(Server server, ClientData client) {
        if(!executeNextTurn(server.world, server.gameData,
                s -> server.broadcast(new ChatMessage(s)),
                s -> server.send(client, new ChatMessage(s)))) return;
        server.broadcast(new SetWorld(server.world));
        server.broadcast(this);
        World.animatePassives(server.world, server.gameData, server.world.teams.getTurn(), (action) -> {
            action.execute(server.world, server.gameData);
        }, (msg) -> {
            System.err.println(msg);
        });
    }

    public static boolean executeNextTurn(World world, GameData gameData, Consumer<String> outputMessage, Consumer<String> outputError) {
        TeamID currentTeam = world.teams.getTurn();
        if(currentTeam == null) {
            world.teams.startTurns();
            outputMessage.accept("Starting turns");
            currentTeam = world.teams.getTurn();
            if(currentTeam == null) {
                outputError.accept("Cannot find next turn without any teams");
                return false;
            }
        } else {
            if(!world.teams.teamEndedTurn(currentTeam)) {
                outputError.accept("Cannot end turn before all clients end turn");
                return false;
            }
            world.teams.nextTurn();
            currentTeam = world.teams.getTurn();
        }
        world.teams.resetClientTurnEnd();
        world.resetGameObjectSpeeds(gameData);
        world.nextVersion();
        outputMessage.accept("New turn for team " + world.teams.getTeamName(currentTeam));
        return true;
    }

    @Override
    public void execute(Game gameResources) {
        gameResources.animatePassives(gameResources.world.teams.getTurn());
    }
}
