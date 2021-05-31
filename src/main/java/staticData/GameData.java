package staticData;

import org.joml.Vector2f;
import org.joml.Vector2i;

import java.io.Serializable;
import java.util.*;

public class GameData implements Serializable {

    private Map<Integer, GameObjectType> gameObjectTypes = new HashMap<>();

    public GameData() {
        int id = addType(new GameObjectType(genUniqueTypeID(),
                "kid",
                "kid.png",
                new Vector2f(0),
                new Vector2f(1),
                new Vector2f(0),
                new Vector2f(1),
                new Vector2f(0),
                new Vector2i(0), new Vector2i(0),
                8.0, 1, new Vector2f(0), 1, true, 1.2f,
                new Vector2f(0.0f)));
        int id2 = addType(new GameObjectType(genUniqueTypeID(),
                "building",
                "kid.png",
                new Vector2f(0.0f, 0.0f),
                new Vector2f(2),
                new Vector2f(0.0f, 0.0f),
                new Vector2f(1.4f, 2.0f),
                new Vector2f(0.0f, 0.0f),
                new Vector2i(0), new Vector2i(1),
                1.0, 1.5f, new Vector2f(0.5f), 2, false, 0,
                new Vector2f(0.5f)));
    }

    public Collection<GameObjectType> getTypes() {
        return gameObjectTypes.values();
    }

    public GameObjectType getType(int id) {
        return gameObjectTypes.get(id);
    }

    public GameObjectType getPlaceholder() {
        return getType(0);
    }

    public GameObjectType getBuildingPlaceholder() {
        return getType(1);
    }

    public int genUniqueTypeID() {
        for(int i = 0;;++i) {
            if(gameObjectTypes.get(i) == null) {
                return i;
            }
        }
    }

    public int addType(GameObjectType type) {
        if(gameObjectTypes.get(type.getUniqueID()) != null) {
            throw new RuntimeException("Duplicate Unique ID " + type.getUniqueID() + ": " + type.getName());
        }
        gameObjectTypes.put(type.getUniqueID(), type);
        return type.getUniqueID();
    }

    public void fixMapKeys() {
        Map<Integer, GameObjectType> oldMap = this.gameObjectTypes;
        this.gameObjectTypes = new HashMap<>();
        for(GameObjectType type : oldMap.values()) {
            addType(type);
        }
    }
}
