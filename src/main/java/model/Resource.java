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

    public static boolean canAfford(Map<ResourceID, Integer> current, Map<ResourceID, Integer> required) {
        for(ResourceID resourceID : required.keySet()) {
            Integer currentCount = current.get(resourceID);
            if(currentCount == null) currentCount = 0;
            if(required.get(resourceID) > currentCount) {
                return false;
            }
        }
        return true;
    }

    public static void subtractResources(Map<ResourceID, Integer> subtractFrom, Map<ResourceID, Integer> subtractAmount) {
        for(ResourceID resourceID : subtractAmount.keySet()) {
            Integer currentCount = subtractFrom.get(resourceID);
            if(currentCount == null) currentCount = 0;
            currentCount -= subtractAmount.get(resourceID);
            subtractFrom.put(resourceID, currentCount);
        }
    }

    public static void addResources(Map<ResourceID, Integer> addTo, Map<ResourceID, Integer> addAmount) {
        for(ResourceID resourceID : addAmount.keySet()) {
            Integer currentCount = addTo.get(resourceID);
            if(currentCount == null) currentCount = 0;
            currentCount += addAmount.get(resourceID);
            addTo.put(resourceID, currentCount);
        }
    }
}
