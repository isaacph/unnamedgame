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
        executeNextTurn(server.world, server.gameData,
                s -> server.broadcast(new ChatMessage(s)),
                s -> server.send(client, new ChatMessage(s)));
        List<GameObjectID> passivers = World.getTeamPassives(server.world.teams.getTurn(), server.world, server.gameData);
        for(GameObjectID id : passivers) {
            Set<AbilityComponent> passives = server.gameData.getType(server.world.gameObjects.get(id).type).getPassives();
            for(AbilityComponent passive : passives) {
                Action action = AbilityOrganizer.abilityPassiveCreator.get(passive.getTypeID()).create(passive.getID(), id);
                if(action.validate(null, server.world, server.gameData)) {
                    action.execute(server.world, server.gameData);
                } else {
                    System.err.println("Failed to execute passive ability: " + passive.getID());
                }
            }
        }
        //server.broadcast(new SetWorld(server.world));
        server.broadcast(this);
    }

    public static void executeNextTurn(World world, GameData gameData, Consumer<String> outputMessage, Consumer<String> outputError) {
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
        world.resetGameObjectSpeeds(gameData);
        world.nextVersion();
        outputMessage.accept("New turn for team " + world.teams.getTeamName(currentTeam));
    }

    @Override
    public void execute(Game gameResources) {
        executeNextTurn(gameResources.world, gameResources.gameData, s -> gameResources.chatbox.println(s), s -> gameResources.chatbox.println(s));
        gameResources.animatePassives(gameResources.world.teams.getTurn());
    }
}
