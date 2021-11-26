package game;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VisualDataTypeFactory {

    private Map<String, ComponentCreator<Display>> displayCreators = new HashMap<>();
    private Map<String, ComponentCreator<ClickBoxComponent>> clickboxCreators = new HashMap<>();
    private Map<String, ComponentCreator<ResourceDisplay>> resourceCreators = new HashMap<>();

    public VisualDataTypeFactory() {
        displayCreators.put("simple", SimpleDisplay::new);
        displayCreators.put("multi", MultiDisplay::new);
        clickboxCreators.put("aabb", AABBClickBox::new);
        resourceCreators.put("simple", ResourceSimpleDisplay::new);
    }

    public Display makeDisplay(JSONObject obj) {
        if(obj == null) throw new RuntimeException("Visual data missing key: display");
        return displayCreators.get(obj.getString("type")).makeComponent(obj);
    }

    public ClickBoxComponent makeClickbox(JSONObject obj) {
        if(obj == null) throw new RuntimeException("Visual data missing key: clickbox");
        return clickboxCreators.get(obj.getString("type")).makeComponent(obj);
    }

    public ResourceDisplay makeResourceDisplay(JSONObject obj) {
        if(obj == null) throw new RuntimeException("Visual data resource missing key: display");
        return resourceCreators.get(obj.getString("type")).makeComponent(obj);
    }

    private interface ComponentCreator<ComponentType> {
        ComponentType makeComponent(JSONObject obj);
    }
}
