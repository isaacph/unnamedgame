package game;

import model.Direct;
import model.GameObjectType;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.json.JSONObject;
import render.Texture;

public class ResourceSimpleDisplay implements ResourceDisplay {

    @Direct
    private String texture;
    @Direct private float size;
    @Direct private Vector2f offset;

    public ResourceSimpleDisplay(JSONObject obj) {
        GameObjectType.assertString(obj.getString("type"), getType());
        texture = obj.getString("texture");
        size = obj.getFloat("size");
        offset = new Vector2f(obj.getFloat("offsetX"), obj.getFloat("offsetY"));
    }

    @Override
    public String getType() {
        return "simple";
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("type", getType());
        obj.put("texture", texture);
        obj.put("size", size);
        obj.put("offsetX", offset.x);
        obj.put("offsetY", offset.y);
        return obj;
    }

    @Override
    public String getTexture() {
        return texture;
    }

    @Override
    public float getSizeMultiplier() {
        return size;
    }

    @Override
    public Vector2fc getOffset() {
        return offset;
    }
}
