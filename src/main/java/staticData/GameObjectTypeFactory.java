package staticData;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GameObjectTypeFactory {

    private Map<String, ComponentCreator<Display>> displayCreators = new HashMap<>();
    private Map<String, ComponentCreator<ClickBoxData>> clickboxCreators = new HashMap<>();
    private Map<String, ComponentCreator<Shape>> shapeCreators = new HashMap<>();

    public GameObjectTypeFactory() {
        displayCreators.put("simple", SimpleDisplay::new);
        clickboxCreators.put("aabb", AABBClickBox::new);
        shapeCreators.put("square", SquareShape::new);
    }

    public Display makeDisplay(JSONObject obj) {
        if(obj == null) throw new RuntimeException("Missing key: display");
        return displayCreators.get(obj.getString("type")).makeComponent(obj);
    }

    public ClickBoxData makeClickbox(JSONObject obj) {
        if(obj == null) throw new RuntimeException("Missing key: clickbox");
        return clickboxCreators.get(obj.getString("type")).makeComponent(obj);
    }

    public Shape makeShape(JSONObject obj) {
        if(obj == null) throw new RuntimeException("Missing key: shape");
        return shapeCreators.get(obj.getString("type")).makeComponent(obj);
    }

    public GameObjectType makeGameObjectType(JSONObject obj) {
        return new GameObjectType(obj, this);
    }

    public interface ComponentCreator<ComponentType> {
        ComponentType makeComponent(JSONObject obj);
    }
}
