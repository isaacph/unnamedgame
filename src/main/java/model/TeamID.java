package model;

import org.json.JSONObject;

import java.io.Serializable;

public class TeamID implements Serializable {

    public static final TeamID NEUTRAL = new TeamID(0);

    private final int id;

    private TeamID(int init) {
        this.id = init;
    }

    public TeamID(TeamID other) {
        this.id = other.id;
    }

    public TeamID(JSONObject obj) {
        this.id = obj.getInt("id");
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Team" + id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TeamID && ((TeamID) other).id == id;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        return obj;
    }

    public static class Generator implements Serializable {

        private int generatorNumber;

        public Generator() {
            generatorNumber = 0;
        }

        public TeamID generate() {
            return new TeamID(++generatorNumber);
        }
    }
}
