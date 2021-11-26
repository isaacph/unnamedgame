package model;

import model.abilities.AbilityComponent;
import org.joml.Vector2i;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

public class GameObjectType implements Serializable {

    private GameObjectTypeID uniqueID;
    private float speed;
    private float health;
    private Shape shape;
    private List<AbilityComponent> abilities;
    private Map<Integer, AbilityComponent> slotAbility;
    private Set<AbilityComponent> passives = new HashSet<>();
    private Map<AbilityID, AbilityComponent> abilityMap;
    private boolean neutral;
    private TypeCollider collider;
    private boolean targetable;
    private Map<ResourceID, Integer> resources;

    public GameObjectType(JSONObject obj, GameObjectTypeFactory factory) {
        uniqueID = new GameObjectTypeID(obj.getString("name"));
        shape = factory.makeShape(obj.getJSONObject("shape"));
        collider = factory.makeCollider(obj.getJSONObject("collider"));
        if(obj.has("neutral")) {
            neutral = obj.getBoolean("neutral");
        } else neutral = false;
        abilities = new ArrayList<>();
        slotAbility = new HashMap<>();
        abilityMap = new HashMap<>();
        health = obj.getFloat("health");
        speed = obj.getFloat("speed");
        if(obj.has("targetable")) targetable = obj.getBoolean("targetable");
        else targetable = true;
        if(!neutral) {
            JSONArray arr = new JSONArray(obj.getJSONArray("abilities"));
            for(int i = 0; i < arr.length(); ++i) {
                AbilityComponent ability = factory.makeAbility(arr.getJSONObject(i), uniqueID);
                abilities.add(ability);
                if(ability.getSlot() != AbilityComponent.NO_SLOT) {
                    slotAbility.put(ability.getSlot(), ability);
                }
                if(ability.isPassive()) {
                    passives.add(ability);
                }
                abilityMap.put(ability.getID(), ability);
            }
        }
        resources = new HashMap<>();
        if(obj.has("resources")) {
            JSONObject resObj = obj.getJSONObject("resources");
            for(String key : resObj.keySet()) {
                resources.put(new ResourceID(key), resObj.getInt(key));
            }
        }
    }

    public String getName() {
        return uniqueID.getName();
    }

    public GameObjectTypeID getUniqueID() {
        return uniqueID;
    }

    public double getSpeed() {
        return speed;
    }

    public float getMaxHealth() { return health; }

    public AbilityComponent getAbility(int slot) {
        if(!slotAbility.containsKey(slot)) return null;
        return slotAbility.get(slot);
    }

    public AbilityComponent getAbility(AbilityID id) {
        if(!abilityMap.containsKey(id)) return null;
        return abilityMap.get(id);
    }

    public boolean isNeutral() {
        return neutral;
    }

    public boolean isTargetable() {
        return targetable;
    }

    public Collider getCollider() {
        return collider;
    }

//    public boolean hasAbility(AbilityTypeID abilityTypeID) {
//        return abilityIDMap.containsKey(abilityTypeID);
//    }
//
//    public AbilityComponent getAbility(AbilityTypeID abilityTypeID) {
//        if(!hasAbility(abilityTypeID)) return null;
//        return abilityIDMap.get(abilityTypeID);
//    }
//
//    @SuppressWarnings("unchecked")
//    public <T extends AbilityComponent> T getAbility(Class<T> tClass) {
//        AbilityTypeID abilityTypeID = classAbilityIDMap.get(tClass);
//        if(abilityTypeID == null) return null;
//        return (T) getAbility(abilityTypeID);
//    }

    public <T extends AbilityComponent> AbilityID getFirstAbilityWithType(Class<T> tClass) {
        for(int slot : slotAbility.keySet()) {
            AbilityComponent comp = slotAbility.get(slot);
            if(comp.getClass().equals(tClass)) {
                return comp.getID();
            }
        }
        return null;
    }

    public Set<AbilityComponent> getPassives() {
        return Collections.unmodifiableSet(this.passives);
    }

    @Override
    public String toString() {
        return uniqueID.toString();
    }

    /** Used by contained object constructors to ensure that the "types" of the json objects passed
     * to their constructors match their class required types */
    public static void assertString(String givenType, String assertType) throws AssertionError {
        if(!givenType.equals(assertType)) {
            throw new AssertionError("Wrong object type! Should have been checked earlier! (type: "
                    + givenType + ", should be " + assertType + ")");
        }
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("name", uniqueID.getName());
        obj.put("speed", speed);
        obj.put("shape", shape.toJSON());
        obj.put("collider", collider.toJSON());
        obj.put("health", health);

        JSONArray abArray = new JSONArray();
        for(AbilityComponent abilityComponent : abilities) {
            abArray.put(abilityComponent.toJSON());
        }
        obj.put("abilities", abArray);

        if(!resources.isEmpty()) {
            JSONObject res = new JSONObject();
            for(ResourceID resourceID : resources.keySet()) {
                res.put(resourceID.getName(), resources.get(resourceID));
            }
            obj.put("resources", res);
        }

        return obj;
    }

    public Set<Vector2i> getRelativeOccupiedTiles() {
        return shape.getRelativeOccupiedTiles();
    }

    public Map<ResourceID, Integer> getResources() {
        return Collections.unmodifiableMap(resources);
    }
}
