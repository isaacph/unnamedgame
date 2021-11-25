package game;

import model.GameObjectTypeID;
import model.ResourceID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lwjgl.system.CallbackI;

import java.util.*;
import java.util.function.Consumer;

/**
 * TODO: create GameObjectTypeID and ResourceID provider to restore the abstraction away from strings
 */
public class VisualData {

    private final List<VisualDataType> types;
    private final Map<GameObjectTypeID, VisualDataType> typeMap;
    private final Map<ResourceID, VisualDataResourceType> resourceTypeMap;

    public VisualData() {
        types = new ArrayList<>();
        typeMap = new HashMap<>();
        resourceTypeMap = new HashMap<>();
    }

    private void clear() {
        types.clear();
        typeMap.clear();
        resourceTypeMap.clear();
    }

    public boolean fromJSON(JSONObject obj, Consumer<RuntimeException> errorHandler) {
        try {
            VisualDataTypeFactory factory = new VisualDataTypeFactory();
            this.clear();
            JSONArray array = obj.getJSONArray("types");
            for(int i = 0; i < array.length(); ++i) {
                JSONObject obj2 = array.getJSONObject(i);
                VisualDataType type = new VisualDataType(obj2, factory);
                types.add(type);
                typeMap.put(type.getID(), type);
            }
            JSONArray resArray = obj.getJSONArray("resources");
            for(int i = 0; i < resArray.length(); ++i) {
                JSONObject obj2 = resArray.getJSONObject(i);
                VisualDataResourceType type = new VisualDataResourceType(obj2, factory);
                resourceTypeMap.put(type.getID(), type);
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
        JSONArray arr = new JSONArray();
        for(VisualDataType type : types) {
            arr.put(type.toJSON());
        }
        obj.put("types", arr);
        JSONArray resArr = new JSONArray();
        for(VisualDataResourceType type : resourceTypeMap.values()) {
            resArr.put(type.toJSON());
        }
        obj.put("resources", resArr);
        return obj;
    }

    public VisualDataType getType(GameObjectTypeID id) {
        return typeMap.get(id);
    }

    public VisualDataResourceType getResourceType(ResourceID id) {
        return resourceTypeMap.get(id);
    }

    public Collection<ResourceID> getLoadedResources() {
        return resourceTypeMap.keySet();
    }
}
