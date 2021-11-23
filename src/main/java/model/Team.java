package model;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;

/** Contains current team status info
 *  I honestly have no clue why we have getters and setters here
 *  Just go with it I guess
 */
public class Team {

    private final TeamID teamID;
    private String name;
    private final Set<ClientID> clients;
    private final Vector3f color;

    public Team(TeamID teamID, Vector3fc defaultColor) {
        this.name = teamID.toString();
        this.teamID = teamID;
        this.clients = new HashSet<>();
        this.color = new Vector3f(defaultColor); // TODO correctly default initialize
    }

    public List<ClientID> getClients() {
        return new ArrayList<>(clients);
    }

    public Vector3f getColor() {
        return new Vector3f(color);
    }

    public void setColor(Vector3fc color) {
        this.color.set(color);
    }

    public boolean hasClient(ClientID client) {
        return clients.contains(client);
    }

    public TeamID getID() {
        return teamID;
    }

    public void removeClients(Collection<ClientID> remove) {
        clients.removeAll(remove);
    }

    public void addClient(ClientID client) {
        clients.add(client);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
