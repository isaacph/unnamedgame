package model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Consumer;

public class GameData {

    private final ArrayList<GameObjectType> types = new ArrayList<>();
    private final Map<GameObjectTypeID, GameObjectType> typeMap = new HashMap<>();

    private GameObjectTypeID kidID, buildingID;

    public GameData() {
        /*kidID = addType(new GameObjectType(new GameObjectTypeID("kid"),
                "kid.png",
                new Vector2f(0),
                new Vector2f(1),
                new Vector2f(0),
                new Vector2f(1),
                new Vector2f(0),
                new Vector2i(0), new Vector2i(0),
                8.0, 1, new Vector2f(0), 1, true, 1.2f,
                new Vector2f(0.0f)));
        buildingID = addType(new GameObjectType(new GameObjectTypeID("building"),
                "kid.png",
                new Vector2f(0.0f, 0.0f),
                new Vector2f(2),
                new Vector2f(0.0f, 0.0f),
                new Vector2f(1.4f, 2.0f),
                new Vector2f(0.0f, 0.0f),
                new Vector2i(0), new Vector2i(1),
                1.0, 1.5f, new Vector2f(0.5f), 2, false, 0,
                new Vector2f(0.5f)));*/
        kidID = new GameObjectTypeID("kid");
        buildingID = new GameObjectTypeID("building");
    }

    private GameObjectTypeID addType(GameObjectType type) {
        types.add(type);
        typeMap.put(type.getUniqueID(), type);
        return type.getUniqueID();
    }

    public GameObjectType getType(GameObjectTypeID id) {
        return typeMap.get(id);
    }

    public List<GameObjectType> getTypes() {
        return new ArrayList<>(types);
    }

    private void updateMap() {
        typeMap.clear();
    }

    public boolean fromJSON(JSONObject json, Consumer<RuntimeException> errorHandler) {
        try {
            typeMap.clear();
            types.clear();
            GameObjectTypeFactory factory = new GameObjectTypeFactory();

            JSONArray types = json.getJSONArray("types");
            for(int i = 0; i < types.length(); ++i) {
                JSONObject obj = types.getJSONObject(i);
                addType(factory.makeGameObjectType(obj));
            }
            return true;
        } catch(RuntimeException e) {
            if(errorHandler != null) {
                errorHandler.accept(e);
                return false;
            } else throw e;
        }
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for(GameObjectType type : types)
        {
            array.put(type.toJSON());
        }
        obj.put("types", array);
        return obj;
    }
}
