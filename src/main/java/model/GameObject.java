package model;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GameObject implements Serializable {

    public final GameObjectID uniqueID;
    public final GameObjectTypeID type;
    public int x, y;
    public TeamID team;
    public float health;
    public boolean alive;
    public boolean targetable;
    public double speedLeft;
    public final Map<AbilityID, Integer> usagesLeft = new HashMap<>();
    public final Map<AbilityID, Integer> cooldown = new HashMap<>();

    public GameObject(GameObjectID uniqueID, GameObjectTypeID type, TeamID team) {
        this.uniqueID = uniqueID;
        this.type = type;
        this.team = team;
        this.health = 0;
        this.speedLeft = 0;
        this.alive = false;
        this.targetable = true;
    }

    public GameObject(JSONObject initObj) {
        this.uniqueID = new GameObjectID(initObj.getJSONObject("uniqueID"));
        this.type = new GameObjectTypeID(initObj.getJSONObject("type"));
        this.x = initObj.getInt("x");
        this.y = initObj.getInt("y");
        this.team = new TeamID(initObj.getJSONObject("team"));
        this.health = initObj.getFloat("health");
        this.speedLeft = initObj.getDouble("speedLeft");
        this.alive = initObj.getBoolean("alive");
        this.targetable = initObj.getBoolean("targetable");
        JSONObject abilitiesData = initObj.getJSONObject("abilities");
        for (String key : abilitiesData.keySet()) {
            JSONObject abilityData = initObj.getJSONObject(key);
            AbilityID ability = new AbilityID(key);
            usagesLeft.put(ability, abilityData.getInt("usagesLeft"));
            cooldown.put(ability, abilityData.getInt("cooldown"));
        }
    }

    public JSONObject toInitJSON() {
        JSONObject obj = new JSONObject();
        obj.put("uniqueID", uniqueID.toJSON());
        obj.put("type", type.toJSON());
        obj.put("x", x);
        obj.put("y", y);
        obj.put("team", team.toJSON());
        obj.put("health", health);
        obj.put("speedLeft", speedLeft);
        obj.put("alive", alive);
        obj.put("targetable", targetable);
        JSONObject abilitiesData = new JSONObject();
        for (AbilityID abilityID : usagesLeft.keySet()) {
            assert cooldown.containsKey(abilityID);
            JSONObject abilityData = new JSONObject();
            abilityData.put("usagesLeft", usagesLeft.get(abilityID));
            abilityData.put("cooldown", cooldown.get(abilityID));
            abilitiesData.put(abilityID.toString(), abilityData);
        }
        obj.put("abilities", abilitiesData);
        return obj;
    }

    public boolean equals(Object other) {
        if(other instanceof GameObject) {
            return uniqueID.equals(((GameObject) other).uniqueID);
        }
        return false;
    }
}
