package server.commands;

import game.ClientPayload;
import game.Game;
import model.TeamManager;

public class SetTeams implements ClientPayload {

    private TeamManager teamManager;

    public SetTeams(TeamManager tm) {
        teamManager = tm;
    }

    @Override
    public void execute(Game gameResources) {
        gameResources.world.teams = teamManager;
    }
}