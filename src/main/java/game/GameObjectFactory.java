package game;

import staticData.GameObjectType;

import java.util.Random;

public class GameObjectFactory {

    private int uniqueIDCounter = 0;

    /** Will eventually only be used by server. The client should only create
     * objects when it already knows the ID that the server set for it
     */
    public GameObject createGameObject(GameObjectType type) {
        GameObject object = new GameObject(uniqueIDCounter++, type.uniqueID);
        type.initialize(object);
        return object;
    }
}
