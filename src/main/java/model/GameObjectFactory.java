package model;

import org.json.JSONObject;

import java.io.Serializable;

public class GameObjectFactory implements Serializable {

    private GameObjectID.Generator generator = new GameObjectID.Generator();

    /** Will eventually only be used by server. The client should only create
     * objects when it already knows the ID that the server set for it
     */
    public GameObject createGameObject(GameObjectType type, TeamID team, GameData gameData) {
        if(team == null && !type.isNeutral()) return null;
        GameObjectID id = generator.generate();
        if(id == null) return null;
        GameObject object = new GameObject(generator.generate(), type.getUniqueID(), type.isNeutral() ? TeamID.NEUTRAL : team);
        object.alive = true;
        object.health = type.getMaxHealth();
        object.speedLeft = type.getSpeed();
        object.targetable = type.isTargetable();
        return object;
    }

    public GameObjectFactory() {}

    public GameObjectFactory(JSONObject obj) {
        generator = new GameObjectID.Generator(obj);
    }

    public JSONObject toJSON() {
        return generator.toJSON();
    }
}
