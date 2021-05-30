package game;

import org.joml.Vector3f;

import java.io.Serializable;
import java.util.*;

public class TeamManager implements Serializable {

    private static final Vector3f[] DEFAULT_COLORS = {
            new Vector3f(1.0f, 0.0f, 0.0f),
            new Vector3f(0.0f, 1.0f, 0.0f),
            new Vector3f(0.0f, 0.0f, 1.0f),
            new Vector3f(1.0f, 1.0f, 0.0f),
            new Vector3f(0.0f, 1.0f, 1.0f),
            new Vector3f(1.0f, 0.0f, 1.0f),
            new Vector3f(1.0f, 1.0f, 1.0f),
            new Vector3f(0.0f, 0.0f, 0.0f),
    };
    private int colorCounter = 0;

    private List<TeamID> teams = new ArrayList<>();
    private Map<TeamID, List<ClientID>> teamClients = new HashMap<>();
    private Map<TeamID, String> teamName = new HashMap<>();
    private Map<TeamID, Vector3f> teamColor = new HashMap<>();

    public List<ClientID> getTeamClients(TeamID teamID) {
        return new ArrayList<>(teamClients.get(teamID));
    }

    public TeamID getClientTeam(ClientID clientID) {
        if(clientID == null) return null;
        for(TeamID team : teams) {
            if(teamClients.get(team) != null && teamClients.get(team).contains(clientID)) {
                return team;
            }
        }
        return null;
    }

    public void setClientTeam(ClientID clientID, TeamID teamID) {
        TeamID currentTeam = getClientTeam(clientID);
        if(currentTeam != null) {
            teamClients.get(currentTeam).remove(clientID);
        }
        if(teamID != null) {
            teamClients.computeIfAbsent(teamID, k -> new ArrayList<>());
            teamClients.get(teamID).add(clientID);
        }
    }

    public void addTeam(TeamID teamID) {
        teams.add(teamID);
        teamColor.put(teamID, DEFAULT_COLORS[colorCounter++ % DEFAULT_COLORS.length]);
    }

    public String getTeamName(TeamID team) {
        return teamName.get(team);
    }

    public void setTeamName(TeamID team, String name) {
        teamName.put(team, name);
    }

    public void setTeamColor(TeamID team, Vector3f color) {
        teamColor.put(team, color);
    }

    public Vector3f getTeamColor(TeamID team) {
        if(team == null) return new Vector3f(1.0f);
        Vector3f color = teamColor.get(team);
        if(color != null) {
            return color;
        }
        return new Vector3f(1.0f);
    }

    public Collection<TeamID> getTeams() {
        return new ArrayList<>(teams);
    }

    public boolean removeTeam(TeamID team) {
        boolean removed = teams.remove(team);
        teamColor.remove(team);
        teamName.remove(team);
        teamClients.remove(team);
        return removed;
    }

    public TeamID getTeamWithName(String name) {
        if(name == null) return null;
        for(TeamID team : teams) {
            if(teamName.get(team) != null && teamName.get(team).equalsIgnoreCase(name)) {
                return team;
            }
        }
        return null;
    }
}
