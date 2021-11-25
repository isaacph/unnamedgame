package game;

import model.*;
import org.joml.Vector2fc;
import org.json.JSONObject;
import render.RenderComponent;
import render.Texture;
import render.WorldRenderer;

public class VisualDataResourceType {

    private ResourceID id;
    private ResourceDisplay display;
//    private ClickBoxComponent clickbox;

    public VisualDataResourceType(JSONObject obj, VisualDataTypeFactory factory) {
        id = new ResourceID(obj.getString("name"));
        display = factory.makeResourceDisplay(obj.getJSONObject("display"));
        //clickbox = factory.makeClickbox(obj.getJSONObject("clickbox"));
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("name", id.getName());
        obj.put("display", display.toJSON());
//        obj.put("clickbox", clickbox.toJSON());
        return obj;
    }

//    public ClickBox makeClickBox(GameObject obj) {
//        return clickbox.makeClickBox(obj);
//    }

    public ResourceID getID() {
        return id;
    }

    public ResourceDisplay getDisplay() {
        return display;
    }
}
