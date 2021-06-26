package model;

import org.json.JSONObject;

import java.io.Serializable;

public class GameObjectID implements Serializable {

    private final int id;

    private GameObjectID(int id) {
        this.id = id;
    }

    public GameObjectID(GameObjectID other) {
        this.id = other.id;
    }

    public GameObjectID(JSONObject obj) {
        this.id = obj.getInt("id");
    }

    @Override
    public int hashCode() {
        return id;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        return obj;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GameObjectID && ((GameObjectID) other).id == id;
    }

    @Override
    public String toString() {
        return "GameObject:" + id;
    }

    public static class Generator implements Serializable {

        private int nextID = 0;

        public Generator() {

        }

        public Generator(JSONObject obj) {
            nextID = obj.getInt("nextID");
        }

        public GameObjectID generate() {
            return new GameObjectID(nextID++);
        }

        public JSONObject toJSON() {
            JSONObject obj = new JSONObject();
            obj.put("nextID", nextID);
            return obj;
        }
    }
}
