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
    private final ArrayList<TeamID> turnOrder = new ArrayList<>();
    private final Map<ClientID, Boolean> clientEndedTurn = new HashMap<>();
    private int currentTurn = -1;
    public final TeamID.Generator teamIDGenerator = new TeamID.Generator();

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
        turnOrder.add(teamID);
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
        if(!turnOrder.isEmpty()) {
            List<Integer> toRemove = new ArrayList<>();
            for(int i = 0; i < turnOrder.size(); ++i) {
                if(turnOrder.get(i).equals(team)) {
                    toRemove.add(i);
                }
            }
            for(int i = toRemove.size() - 1; i >= 0; --i) {
                turnOrder.remove((int) toRemove.get(i));
            }
        }
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

    public TeamID getTurn() {
        if(turnOrder.isEmpty()) return null;
        if(currentTurn < 0 || currentTurn >= turnOrder.size()) return null;
        return turnOrder.get(currentTurn);
    }

    public TeamID nextTurn() {
        if(turnOrder.isEmpty()) return null;
        currentTurn = (currentTurn + 1) % turnOrder.size();
        return getTurn();
    }

    public List<TeamID> getTurnOrder() {
        return new ArrayList<>(turnOrder);
    }

    public void setTurnOrder(List<TeamID> order) {
        this.turnOrder.clear();
        for(TeamID id : order) {
            turnOrder.add(id);
        }
    }

    public void startTurns() {
        currentTurn = 0;
    }

    public void endClientTurn(ClientID client) {
        clientEndedTurn.put(client, true);
    }

    private boolean clientEndedTurn(ClientID client) {
        Boolean ended = clientEndedTurn.get(client);
        if(ended == null) return false;
        return ended;
    }

    public void resetClientTurnEnd() {
        clientEndedTurn.clear();
    }

    public boolean teamEndedTurn(TeamID team) {
        if(team == null) return true;
        List<ClientID> clients = teamClients.get(team);
        if(clients == null || clients.isEmpty()) return true;
        for(ClientID client : clients) {
            if(!clientEndedTurn(client)) {
                return false;
            }
        }
        return true;
    }

    public boolean isClientsTurn(ClientID client) {
        if(client == null) return false;
        TeamID team = getClientTeam(client);
        if(team == null) return false;
        TeamID turn = getTurn();
        if(turn == null) return false;
        if(clientEndedTurn(client)) return false;
        return getClientTeam(client).equals(turn);
    }
}
