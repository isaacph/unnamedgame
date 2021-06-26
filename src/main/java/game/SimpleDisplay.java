package game;

import model.*;
import org.joml.Vector2f;
import org.json.JSONObject;
import render.RenderComponent;
import render.TeamTextureComponent;
import render.WorldRenderer;

public class SimpleDisplay implements Display {

    @Direct
    private String texture;
    @Direct private float size;
    @Direct private Vector2f offset;
    @Direct private float circleSize;
    @Direct private Vector2f circleOffset;
    @Direct private Vector2f centerOffset;

    public SimpleDisplay(JSONObject obj) {
        GameObjectType.assertString(obj.getString("type"), getType());
        texture = obj.getString("texture");
        size = obj.getFloat("size");
        offset = new Vector2f(obj.getFloat("offsetX"), obj.getFloat("offsetY"));
        circleSize = obj.getFloat("circleSize");
        circleOffset = new Vector2f(obj.getFloat("circleOffsetX"), obj.getFloat("circleOffsetY"));
        centerOffset = new Vector2f(obj.getFloat("centerOffsetX"), obj.getFloat("centerOffsetY"));
    }

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
        obj.put("circleSize", circleSize);
        obj.put("circleOffsetX", circleOffset.x);
        obj.put("circleOffsetY", circleOffset.y);
        obj.put("centerOffsetX", centerOffset.x);
        obj.put("centerOffsetY", centerOffset.y);
        return obj;
    }

    @Override
    public RenderComponent makeRenderComponent(GameObjectID gameObjectID, World world, GameData gameData,
                                               WorldRenderer.GameObjectTextures textureLibrary) {
        return new TeamTextureComponent(gameObjectID, world, gameData, textureLibrary,
                texture, new Vector2f(offset), new Vector2f(size), new Vector2f(centerOffset),
                new Vector2f(circleOffset), circleSize);
    }
}
