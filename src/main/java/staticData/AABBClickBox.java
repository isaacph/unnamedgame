package staticData;

import game.Camera;
import game.ClickBox;
import game.GameObject;
import org.joml.Vector2f;
import org.json.JSONException;
import org.json.JSONObject;

public class AABBClickBox implements ClickBoxData {

    private Vector2f size;
    private Vector2f offset;
    private float depthOffset;

    public AABBClickBox(JSONObject obj) throws JSONException {
        GameObjectType.assertString(obj.getString("type"), getType());
        size = new Vector2f(obj.getFloat("sizeX"), obj.getFloat("sizeY"));
        offset = new Vector2f(obj.getFloat("offsetX"), obj.getFloat("offsetY"));
        depthOffset = obj.getFloat("depthOffset");
    }

    @Override
    public String getType() {
        return "aabb";
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        obj.put("type", getType());
        obj.put("sizeX", size.x);
        obj.put("sizeY", size.y);
        obj.put("offsetX", offset.x);
        obj.put("offsetY", offset.y);
        obj.put("depthOffset", depthOffset);
        return obj;
    }

    @Override
    public ClickBox makeClickBox(GameObject gameObject) {
        return new ClickBox(gameObject.uniqueID, gameObject.team,
                Camera.worldToViewSpace(new Vector2f(gameObject.x, gameObject.y))
                        .add(offset)
                        .sub(new Vector2f(size).div(2)),
                Camera.worldToViewSpace(new Vector2f(gameObject.x, gameObject.y))
                        .add(offset)
                        .add(new Vector2f(size).div(2)),
                new Vector2f(0, depthOffset));
    }
}
