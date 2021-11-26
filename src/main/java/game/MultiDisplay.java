package game;

import model.*;
import org.joml.Vector2f;
import org.json.JSONArray;
import org.json.JSONObject;
import render.RenderComponent;
import render.TeamTextureComponent;
import render.WorldRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiDisplay implements Display {

    @Direct private JSONArray renders;
    @Direct private float circleSize;
    @Direct private Vector2f circleOffset;
    @Direct private Vector2f centerOffset;

    public MultiDisplay(JSONObject obj) {
        GameObjectType.assertString(obj.getString("type"), getType());
        this.renders = obj.getJSONArray("renders");
        for(int i = 0; i < renders.length(); ++i) {
            JSONObject renderObj = renders.getJSONObject(i);
            String[] requiredKeys = {"texture", "size", "offsetX", "offsetY", "depthOffset"};
            for(String req : requiredKeys) {
                if(!renderObj.has(req)) {
                    throw new AssertionError("No " + req + " key in render info " + i);
                }
            }
        }
        circleSize = obj.getFloat("circleSize");
        circleOffset = new Vector2f(obj.getFloat("circleOffsetX"), obj.getFloat("circleOffsetY"));
        centerOffset = new Vector2f(obj.getFloat("centerOffsetX"), obj.getFloat("centerOffsetY"));
    }

    public String getType() {
        return "multi";
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("type", getType());
        obj.put("renders", renders);
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
        List<TeamTextureComponent.RenderInfo> renderInfo = new ArrayList<>();
        for(int i = 0; i < renders.length(); ++i) {
            JSONObject obj = renders.getJSONObject(i);
            TeamTextureComponent.RenderInfo info = new TeamTextureComponent.RenderInfo(
                    textureLibrary.getTexture(obj.getString("texture")),
                    obj.getFloat("size"),
                    new Vector2f(
                            obj.getFloat("offsetX"),
                            obj.getFloat("offsetY")
                    ),
                    obj.getFloat("depthOffset")
            );
            renderInfo.add(info);
        }
        return new TeamTextureComponent(gameObjectID,
                world,
                gameData,
                renderInfo,
                new Vector2f(centerOffset),
                new Vector2f(circleOffset), circleSize);
    }
}
