package model.abilities;

import model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public abstract class AbilityImpl implements AbilityComponent {

    @Direct private AbilityTypeID type;
    @Direct private int slot;
    @Direct.Optional private JSONObject cost;
    private GameObjectTypeID parentType;
    private Map<ResourceID, Integer> resourceCost;
    private double speedCost;

    public AbilityImpl(JSONObject obj, AbilityTypeID abilityTypeID, GameObjectTypeID parentType) {
        GameObjectType.assertString(obj.getString("type"), abilityTypeID.getName());
        Set<String> defaults =
                ReflectionJSON.extract(obj, this, "Ability: " + parentType.getName() + "." + abilityTypeID.getName());
        this.parentType = parentType;
        this.type = abilityTypeID;

        if(defaults.contains("cost")) {
            cost = new JSONObject();
        }
        this.speedCost = 0;
        this.resourceCost = Collections.emptyMap();
        for(String key : cost.keySet()) {
            if(key.equals("speed")) {
                this.speedCost = cost.getDouble(key);
            } else if(key.equals("resources")) {
                JSONObject costs = cost.getJSONObject(key);
                this.resourceCost = new HashMap<>();
                for(String resKey : costs.keySet()) {
                    resourceCost.put(new ResourceID(resKey), costs.getInt(resKey));
                }
            } else {
                throw new AssertionError("Invalid key: " + key + ". Ability: " + parentType.getName() + "." + abilityTypeID.getName());
            }
        }
    }

    @Override
    public AbilityTypeID getTypeID() {
        return type;
    }

    @Override
    public AbilityID getID() {
        return new AbilityID(parentType, type, slot);
    }

    @Override
    public JSONObject toJSON() {
        return ReflectionJSON.makeJSON(this);
    }

    @Override
    public int getSlot() {
        return slot;
    }

    public double getSpeedCost() {
        return speedCost;
    }

    /**
     * @return Return all resources that a use of this ability costs, in an unmodifiable map.
     */
    public Map<ResourceID, Integer> getResourceCost() {
        return Collections.unmodifiableMap(resourceCost);
    }

    @Override
    public boolean isPassive() {
        return false;
    }
}
