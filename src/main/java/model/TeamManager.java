package model;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.Serializable;
import java.util.*;

/** Also the "turn manager" */
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

//    private final List<TeamID> teams = new ArrayList<>();
    private final Map<TeamID, Team> teams = new HashMap<>();
//    private final Map<TeamID, List<ClientID>> teamClients = new HashMap<>();
//    private final Map<TeamID, String> teamName = new HashMap<>();
//    private final Map<TeamID, Vector3f> teamColor = new HashMap<>();
    private final ArrayList<TeamID> turnOrder = new ArrayList<>();
    private final Map<ClientID, Boolean> clientEndedTurn = new HashMap<>();
    private int currentTurn = -1;
    public final TeamID.Generator teamIDGenerator = new TeamID.Generator();

    public TeamManager() {
        clear();
    }

    public List<ClientID> getTeamClients(TeamID teamID) {
        Team team = teams.get(teamID);
        if(team == null) return new ArrayList<>();
        return team.getClients();
    }

    public TeamID getClientTeam(ClientID clientID) {
        if(clientID == null) return null;
        for(Team team : teams.values()) {
            if(team.hasClient(clientID)) {
                return team.getID();
            }
        }
        return null;
    }

    public void setClientTeam(ClientID clientID, TeamID teamID) {
        TeamID currentTeam = getClientTeam(clientID);
        if(currentTeam != null) {
            teams.get(currentTeam).removeClients(Collections.singletonList(clientID));
        }

        if(teamID != null) {
            Team team = teams.get(teamID);
            if(team == null) {
                addTeam(teamID);
                team = teams.get(teamID);
            }
            team.addClient(clientID);
        }
    }

    public void addTeam(TeamID teamID) {
        teams.put(teamID, new Team(teamID, nextDefaultColor()));
        turnOrder.add(teamID);
    }

    private Vector3fc nextDefaultColor() {
        return DEFAULT_COLORS[colorCounter++ % DEFAULT_COLORS.length];
    }

    public String getTeamName(TeamID teamID) {
        Team team = teams.get(teamID);
        if(team == null) return null;
        return team.getName();
    }

    public void setTeamName(TeamID teamID, String name) {
        Team team = teams.get(teamID);
        if(team != null) team.setName(name);
    }

    public void setTeamColor(TeamID teamID, Vector3fc color) {
        Team team = teams.get(teamID);
        if(team != null) team.setColor(color);
    }

    public Vector3f getTeamColor(TeamID teamID) {
        Team team = teams.get(teamID);
        if(team != null) return team.getColor();
        return new Vector3f(1);
    }

    public Map<ResourceID, Integer> getTeamResources(TeamID teamID) {
        Team team = teams.get(teamID);
        if(team != null) return team.getResources();
        return Collections.emptyMap();
    }

    public int getTeamResource(TeamID teamID, ResourceID resourceID) {
        Team team = teams.get(teamID);
        if(team == null) return 0;
        return team.getResource(resourceID);
    }

    public void setTeamResource(TeamID teamID, ResourceID resourceID, int amount) {
        Team team = teams.get(teamID);
        if(team != null) team.setResource(resourceID, amount);
    }

    public void setTeamResources(TeamID teamID, Map<ResourceID, Integer> newResources) {
        Team team = teams.get(teamID);
        if(team != null) team.setResources(newResources);
    }

    public boolean isTeam(TeamID teamID) {
        return teams.get(teamID) != null;
    }

    public Collection<TeamID> getTeams() {
        return new ArrayList<>(teams.keySet());
    }

    public boolean removeTeam(TeamID teamID) {
        Team removed = teams.remove(teamID);
        if(!turnOrder.isEmpty()) {
            List<Integer> toRemove = new ArrayList<>();
            for(int i = 0; i < turnOrder.size(); ++i) {
                if(turnOrder.get(i).equals(teamID)) {
                    toRemove.add(i);
                }
            }
            for(int i = toRemove.size() - 1; i >= 0; --i) {
                turnOrder.remove((int) toRemove.get(i));
            }
        }
        return removed != null;
    }

    public TeamID getTeamWithName(String name) {
        if(name == null) return null;
        for(Team team : teams.values()) {
            if(team.getName().equalsIgnoreCase(name)) {
                return team.getID();
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
        turnOrder.addAll(order);
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
        List<ClientID> clients = getTeamClients(team);
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

    public void clear() {
        teams.clear();
        turnOrder.clear();
        clientEndedTurn.clear();
        currentTurn = -1;
        addTeam(TeamID.NEUTRAL);
        turnOrder.remove(TeamID.NEUTRAL);
        setTeamName(TeamID.NEUTRAL, "Neutral");
    }
}
