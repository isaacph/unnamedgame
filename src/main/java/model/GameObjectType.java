package model;

import org.joml.Vector2i;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

public class GameObjectType implements Serializable {

    private String name;
    private GameObjectTypeID uniqueID;
    private float speed;
    private float health;
    private Shape shape;
    private List<AbilityComponent> abilities;
    private Map<Integer, AbilityComponent> slotAbility;
    private Map<AbilityID, AbilityComponent> abilityIDMap;
    private Map<Class, AbilityID> classAbilityIDMap;

    public GameObjectType(JSONObject obj, GameObjectTypeFactory factory) {
        name = obj.getString("name");
        uniqueID = new GameObjectTypeID(name);
        speed = obj.getFloat("speed");
        health = obj.getFloat("health");
        shape = factory.makeShape(obj.getJSONObject("shape"));
        abilities = new ArrayList<>();
        slotAbility = new HashMap<>();
        abilityIDMap = new HashMap<>();
        classAbilityIDMap = new HashMap<>();
        JSONArray arr = new JSONArray(obj.getJSONArray("abilities"));
        for(int i = 0; i < arr.length(); ++i) {
            AbilityComponent ability = factory.makeAbility(arr.getJSONObject(i));
            abilities.add(ability);
            slotAbility.put(ability.getSlot(), ability);
            abilityIDMap.put(ability.getID(), ability);
            classAbilityIDMap.put(ability.getClass(), ability.getID());
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

    public boolean hasAbility(AbilityID abilityID) {
        return abilityIDMap.containsKey(abilityID);
    }

    public AbilityComponent getAbility(AbilityID abilityID) {
        if(!hasAbility(abilityID)) return null;
        return abilityIDMap.get(abilityID);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbilityComponent> T getAbility(Class<T> tClass) {
        AbilityID abilityID = classAbilityIDMap.get(tClass);
        if(abilityID == null) return null;
        return (T) getAbility(abilityID);
    }

    @Override
    public String toString() {
        return uniqueID.toString();
    }

    public static void assertString(String givenType, String assertType) throws AssertionError {
        if(!givenType.equals(assertType)) {
            throw new AssertionError("Wrong object type! Should have been checked earlier! (type: "
                    + givenType + ", should be " + assertType + ")");
        }
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("speed", speed);
        obj.put("shape", shape.toJSON());
        obj.put("health", health);

        JSONArray abArray = new JSONArray();
        for(AbilityComponent abilityComponent : abilities) {
            abArray.put(abilityComponent.toJSON());
        }
        obj.put("abilities", abArray);

        return obj;
    }

    public Set<Vector2i> getRelativeOccupiedTiles() {
        return shape.getRelativeOccupiedTiles();
    }
}
