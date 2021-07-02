package model;

import org.json.JSONObject;

import java.io.Serializable;

public class GameObjectTypeID implements Serializable {

    private final String nameID;

    public GameObjectTypeID(String nameID) {
        this.nameID = nameID;
    }

    public GameObjectTypeID(GameObjectTypeID other) {
        this.nameID = other.nameID;
    }

    public GameObjectTypeID(JSONObject obj) {
        this.nameID = obj.getString("nameID");
    }

    @Override
    public int hashCode() {
        return nameID.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GameObjectTypeID && ((GameObjectTypeID) other).nameID.equals(nameID);
    }

    public String getName() {
        return nameID;
    }

    @Override
    public String toString() {
        return "GameObjectType:" + nameID;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("nameID", nameID);
        return obj;
    }
}
