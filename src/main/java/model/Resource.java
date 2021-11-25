package model;

import model.abilities.AbilityComponent;
import org.joml.Vector2i;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

public class Resource implements Serializable {

    private ResourceID uniqueID;

    public Resource(JSONObject obj) {
        uniqueID = new ResourceID(obj.getString("name"));
    }

    public String getName() {
        return uniqueID.getName();
    }

    public ResourceID getUniqueID() {
        return uniqueID;
    }

    @Override
    public String toString() {
        return uniqueID.toString();
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("name", uniqueID.getName());

        return obj;
    }
}
