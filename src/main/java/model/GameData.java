package model;

import model.abilities.AbilityComponent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Consumer;

public class GameData {

    private final ArrayList<GameObjectType> types = new ArrayList<>();
    private final Map<GameObjectTypeID, GameObjectType> typeMap = new HashMap<>();
    private final Map<ResourceID, Resource> resourceTypes = new HashMap<>();

    public GameData() {
        this.clear();
    }

    private void clear() {
        typeMap.clear();
        types.clear();
        resourceTypes.clear();
    }

    private GameObjectTypeID addType(GameObjectType type) {
        types.add(type);
        typeMap.put(type.getUniqueID(), type);
        return type.getUniqueID();
    }

    private ResourceID addResource(Resource resource) {
        resourceTypes.put(resource.getUniqueID(), resource);
        return resource.getUniqueID();
    }

    public GameObjectType getType(GameObjectTypeID id) {
        return typeMap.get(id);
    }

    public List<GameObjectType> getTypes() {
        return new ArrayList<>(types);
    }

    public Collection<Resource> getResourceTypes() {
        return new ArrayList<>(resourceTypes.values());
    }

    public Collection<ResourceID> getResourceIDs() {
        return new ArrayList<>(resourceTypes.keySet());
    }

    public Resource getResourceType(ResourceID resourceID) {
        return resourceTypes.get(resourceID);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbilityComponent> T getAbility(Class<T> tClass, AbilityID abilityID) {
        GameObjectType type = getType(abilityID.gameObjectTypeID);
        if(type == null) return null;
        AbilityComponent comp = type.getAbility(abilityID);
        if(comp == null) return null;
        if(!comp.getClass().equals(tClass)) return null;
        return (T) comp;
    }

    public boolean fromJSON(JSONObject json, Consumer<RuntimeException> errorHandler) {
        try {
            this.clear();
            GameObjectTypeFactory factory = new GameObjectTypeFactory();

            JSONArray types = json.getJSONArray("types");
            for(int i = 0; i < types.length(); ++i) {
                JSONObject obj = types.getJSONObject(i);
                addType(factory.makeGameObjectType(obj));
            }

            JSONArray resourceTypes = json.getJSONArray("resources");
            for(int i = 0; i < resourceTypes.length(); ++i) {
                JSONObject obj = resourceTypes.getJSONObject(i);
                addResource(factory.makeResource(obj));
            }
            return true;
        } catch(RuntimeException e) {
            if(errorHandler != null) {
                errorHandler.accept(e);
                return false;
            } else throw e;
        }
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for(GameObjectType type : types)
        {
            array.put(type.toJSON());
        }
        obj.put("types", array);
        JSONArray resArray = new JSONArray();
        for(Resource resource : resourceTypes.values()) {
            resArray.put(resource.toJSON());
        }
        obj.put("resources", resArray);
        return obj;
    }

    public ResourceID getResourceID(String name) {
        if(resourceTypes.get(new ResourceID(name)) != null) {
            return new ResourceID(name);
        }
        return null;
    }
}
