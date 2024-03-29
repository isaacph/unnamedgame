package model;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.Serializable;
import java.util.*;

/** Contains current team status info
 *  I honestly have no clue why we have getters and setters here
 *  Just go with it I guess
 */
public class Team implements Serializable {

    private final TeamID teamID;
    private String name;
    private final Set<ClientID> clients;
    private final Vector3f color;
    private Map<ResourceID, Integer> resources;

    public Team(TeamID teamID, Vector3fc defaultColor) {
        this.name = teamID.toString();
        this.teamID = teamID;
        this.clients = new HashSet<>();
        this.color = new Vector3f(defaultColor); // TODO correctly default initialize
        this.resources = new HashMap<>();
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

    public void setResource(ResourceID resID, int amount) {
        this.resources.put(resID, amount);
    }

    public void setResources(Map<ResourceID, Integer> newRes) {
        for(ResourceID resourceID : newRes.keySet()) {
            resources.put(resourceID, newRes.get(resourceID));
        }
    }

    public int getResource(ResourceID resID) {
        Integer count = this.resources.get(resID);
        if(count == null) return 0;
        return count;
    }

    public Map<ResourceID, Integer> getResources() {
        return Collections.unmodifiableMap(resources);
    }
}
