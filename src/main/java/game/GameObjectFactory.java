package game;

import staticData.GameObjectType;

import java.io.Serializable;
import java.util.Random;

public class GameObjectFactory implements Serializable {

    private GameObjectID.Generator generator = new GameObjectID.Generator();

    /** Will eventually only be used by server. The client should only create
     * objects when it already knows the ID that the server set for it
     */
    public GameObject createGameObject(GameObjectType type, TeamID team) {
        if(team == null) return null;
        GameObject object = new GameObject(generator.generate(), type.getUniqueID(), team, type.getMaxHealth(), type.getBaseSpeed());
        type.initialize(object);
        object.resetSpeed();
        return object;
    }
}
