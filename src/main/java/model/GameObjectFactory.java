package model;

import org.json.JSONObject;

import java.io.Serializable;

public class GameObjectFactory implements Serializable {

    private GameObjectID.Generator generator = new GameObjectID.Generator();

    /** Will eventually only be used by server. The client should only create
     * objects when it already knows the ID that the server set for it
     */
    public GameObject createGameObject(GameObjectType type, TeamID team) {
        if(team == null) return null;
        GameObjectID id = generator.generate();
        if(id == null) return null;
        GameObject object = new GameObject(generator.generate(), type.getUniqueID(), team);
        object.alive = true;
        object.health = type.getMaxHealth();
        object.speedLeft = type.getSpeed();
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
