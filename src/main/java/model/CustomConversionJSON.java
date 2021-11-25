package model;

import org.json.JSONObject;

public class CustomConversionJSON{

    public static <T> Object createFieldFromJSON(Class<T> type, String fieldName, JSONObject source) {
        if(type.equals(GameObjectTypeID.class)) {
            return new GameObjectTypeID(source.getString(fieldName));
        } else if(type.equals(AbilityTypeID.class)) {
            return new AbilityTypeID(source.getString(fieldName));
        } else if(type.equals(JSONObject.class)) {
            return source.getJSONObject(fieldName);
        }
        throw new Error("Field (" + fieldName + ") marked @Direct was of unsupported Object type: " + type.toString());
    }

    public static <T> void putFieldIntoJSON(JSONObject dest, String fieldName, Class<T> type, Object obj) {
        if(type.equals(GameObjectTypeID.class)) {
            GameObjectTypeID id = (GameObjectTypeID) obj;
            dest.put(fieldName, id.getName());
            return;
        }
        if(type.equals(AbilityTypeID.class)) {
            AbilityTypeID id = (AbilityTypeID) obj;
            dest.put(fieldName, id.getName());
            return;
        }
        if(type.equals(JSONObject.class)) {
            JSONObject json = (JSONObject) obj;
            dest.put(fieldName, json);
            return;
        }
        throw new Error("Field (" + fieldName + ") marked @Direct was of unsupported Object type: " + type.toString());
    }
}
