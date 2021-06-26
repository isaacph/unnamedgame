package game;

import model.GameObjectTypeID;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class VisualData {

    private final List<VisualDataType> types;
    private final Map<GameObjectTypeID, VisualDataType> typeMap;

    public VisualData() {
        types = new ArrayList<>();
        typeMap = new HashMap<>();
    }

    public boolean fromJSON(JSONObject obj, Consumer<RuntimeException> errorHandler) {
        try {
            VisualDataTypeFactory factory = new VisualDataTypeFactory();
            types.clear();
            typeMap.clear();
            JSONArray array = obj.getJSONArray("types");
            for(int i = 0; i < array.length(); ++i) {
                JSONObject obj2 = array.getJSONObject(i);
                VisualDataType type = new VisualDataType(obj2, factory);
                types.add(type);
                typeMap.put(type.getID(), type);
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
        return obj;
    }

    public VisualDataType getType(GameObjectTypeID id) {
        return typeMap.get(id);
    }
}
