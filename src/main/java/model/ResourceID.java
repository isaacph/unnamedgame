package model;

import org.json.JSONObject;

import java.io.Serializable;

public class ResourceID implements Serializable {

    private final String nameID;

    public ResourceID(String id) {
        this.nameID = id;
    }

    public ResourceID(ResourceID other) {
        this.nameID = other.nameID;
    }

    public ResourceID(JSONObject obj) {
        this.nameID = obj.getString("name");
    }

    @Override
    public int hashCode() {
        return nameID.hashCode();
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", nameID);
        return obj;
    }

    public String getName() {
        return nameID;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ResourceID && ((ResourceID) other).nameID.equals(nameID);
    }

    @Override
    public String toString() {
        return "Resource:" + nameID;
    }
}
