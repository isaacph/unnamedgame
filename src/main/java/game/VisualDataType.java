package game;

import model.*;
import org.json.JSONObject;
import render.RenderComponent;
import render.WorldRenderer;

public class VisualDataType {

    private GameObjectTypeID id;
    private Display display;
    private ClickBoxComponent clickbox;

    public VisualDataType(JSONObject obj, VisualDataTypeFactory factory) {
        id = new GameObjectTypeID(obj.getString("name"));
        display = factory.makeDisplay(obj.getJSONObject("display"));
        clickbox = factory.makeClickbox(obj.getJSONObject("clickbox"));
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("name", id.getName());
        obj.put("display", display.toJSON());
        obj.put("clickbox", clickbox.toJSON());
        return obj;
    }

    public ClickBox makeClickBox(GameObject obj) {
        return clickbox.makeClickBox(obj);
    }

    public RenderComponent makeRenderComponent(GameObjectID gameObjectID, World world, GameData gameData, WorldRenderer.GameObjectTextures textureLibrary) {
        return display.makeRenderComponent(gameObjectID, world, gameData, textureLibrary);
    }

    public GameObjectTypeID getID() {
        return id;
    }
}
