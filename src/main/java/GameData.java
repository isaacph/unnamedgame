import org.joml.Vector2f;

import java.io.Serializable;
import java.util.*;

public class GameData implements Serializable {

    private Map<Integer, GameObjectType> gameObjectTypes = new HashMap<>();

    public GameData() {
        addType(new GameObjectType(genUniqueTypeID(), "kid", "kid.png", new Vector2f(0), new Vector2f(1)));
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

    public int genUniqueTypeID() {
        for(int i = 0;;++i) {
            if(gameObjectTypes.get(i) == null) {
                return i;
            }
        }
    }

    public void addType(GameObjectType type) {
        if(gameObjectTypes.get(type.uniqueID) != null) {
            throw new RuntimeException("Duplicate Unique ID " + type.uniqueID + ": " + type.name);
        }
        gameObjectTypes.put(type.uniqueID, type);
    }

    public void fixMapKeys() {
        Map<Integer, GameObjectType> oldMap = this.gameObjectTypes;
        this.gameObjectTypes = new HashMap<>();
        for(GameObjectType type : oldMap.values()) {
            addType(type);
        }
    }
}
