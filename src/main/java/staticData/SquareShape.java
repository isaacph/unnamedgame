package staticData;

import org.joml.Vector2i;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class SquareShape implements Shape {

    private int size;

    public SquareShape(JSONObject obj) {
        GameObjectType.assertString(obj.getString("type"), getType());
        size = obj.getInt("size");
    }

    @Override
    public String getType() {
        return "square";
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("type", getType());
        obj.put("size", size);
        return obj;
    }

    public Set<Vector2i> getRelativeOccupiedTiles() {
        Set<Vector2i> tiles = new HashSet<>();
        for(int x = -(size-1)/2; x <= size/2; ++x) {
            for(int y = -(size-1)/2; y <= size/2; ++y) {
                tiles.add(new Vector2i(x, y));
            }
        }
        return tiles;
    }
}
