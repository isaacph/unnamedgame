package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.json.JSONObject;
import staticData.GameObjectTypeID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameObject implements Serializable {

    public final GameObjectID uniqueID;
    public final GameObjectTypeID type;
    public int x, y;
    public TeamID team;
    public float health;
    public double speedLeft;
    public boolean alive;

    public GameObject(GameObjectID uniqueID, GameObjectTypeID type, TeamID team) {
        this.uniqueID = uniqueID;
        this.type = type;
        this.team = team;
        this.health = 0;
        this.speedLeft = 0;
        this.alive = false;
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
        return obj;
    }
}
