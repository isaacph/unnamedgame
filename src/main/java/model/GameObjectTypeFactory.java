package model;

import model.abilities.*;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GameObjectTypeFactory {

    private Map<String, ComponentCreator<Shape>> shapeCreators = new HashMap<>();
    private Map<String, AbilityCreator> abilityCreators = new HashMap<>();
    private Map<String, ComponentCreator<TypeCollider>> colliderCreators = new HashMap<>();

    public GameObjectTypeFactory() {
        shapeCreators.put("square", SquareShape::new);
        colliderCreators.put("block", BlockCollider::new);
        colliderCreators.put("none", EmptyCollider::new);
        abilityCreators.put(MoveAbility.ID.getName(), MoveAbility::new);
        abilityCreators.put(GrowAbility.ID.getName(), GrowAbility::new);
        abilityCreators.put(SpawnAbility.ID.getName(), SpawnAbility::new);
        abilityCreators.put(AttackAbility.ID.getName(), AttackAbility::new);
        abilityCreators.put(DismissAbility.ID.getName(), DismissAbility::new);
        abilityCreators.put(CollectAbility.ID.getName(), CollectAbility::new);
    }

    public Shape makeShape(JSONObject obj) {
        if(obj == null) throw new RuntimeException("Missing key: shape");
        if(shapeCreators.get(obj.getString("type")) == null) throw new RuntimeException("Unknown shape key: " + obj.getString("type"));
        return shapeCreators.get(obj.getString("type")).makeComponent(obj);
    }

    public TypeCollider makeCollider(JSONObject obj) {
//        if(obj == null) return colliderCreators.get("none").makeComponent(new JSONObject("{\"type\": \"none\"}"));
        if(obj == null) throw new RuntimeException("Missing key: collider");
        return colliderCreators.get(obj.getString("type")).makeComponent(obj);
    }

    public AbilityComponent makeAbility(JSONObject obj, GameObjectTypeID type) {
        if(obj == null) throw new RuntimeException("Missing key: ability");
        AbilityCreator creator = abilityCreators.get(obj.getString("type"));
        if(creator == null) throw new RuntimeException("Unknown ability key: " + obj.getString("type"));
        return creator.makeComponent(obj, type);
    }

    public Resource makeResource(JSONObject obj) {
        if(obj == null) throw new RuntimeException("Empty resource?");
        return new Resource(obj);
    }

    public GameObjectType makeGameObjectType(JSONObject obj) {
        return new GameObjectType(obj, this);
    }

    private interface ComponentCreator<ComponentType> {
        ComponentType makeComponent(JSONObject obj);
    }

    private interface AbilityCreator {
        AbilityComponent makeComponent(JSONObject obj, GameObjectTypeID gameObjectTypeID);
    }
}
