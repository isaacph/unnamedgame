package server.commands;

import game.ClientPayload;
import game.GameResources;
import game.TeamManager;

public class SetTeams implements ClientPayload {

    private TeamManager teamManager;

    public SetTeams(TeamManager tm) {
        teamManager = tm;
    }

    @Override
    public void execute(GameResources gameResources) {
        gameResources.world.teams = teamManager;
    }
}