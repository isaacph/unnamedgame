package model;

import org.json.JSONObject;

import java.lang.reflect.Field;

public final class ReflectionJSON {

    public static void extractInto(JSONObject source, Object dest) {
        for(Field field : dest.getClass().getDeclaredFields()) {
            field.getType().toString();
        }
    }
}
