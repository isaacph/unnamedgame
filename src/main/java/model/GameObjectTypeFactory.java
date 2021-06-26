package model;

import model.abilities.AttackAbility;
import model.abilities.GrowAbility;
import model.abilities.MoveAbility;
import model.abilities.SpawnAbility;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GameObjectTypeFactory {

    private Map<String, ComponentCreator<Shape>> shapeCreators = new HashMap<>();
    private Map<String, ComponentCreator<AbilityComponent>> abilityCreators = new HashMap<>();

    public GameObjectTypeFactory() {
        shapeCreators.put("square", SquareShape::new);
        abilityCreators.put(MoveAbility.ID.getName(), MoveAbility::new);
        abilityCreators.put(GrowAbility.ID.getName(), GrowAbility::new);
        abilityCreators.put(SpawnAbility.ID.getName(), SpawnAbility::new);
        abilityCreators.put(AttackAbility.ID.getName(), AttackAbility::new);
    }

    public Shape makeShape(JSONObject obj) {
        if(obj == null) throw new RuntimeException("Missing key: shape");
        return shapeCreators.get(obj.getString("type")).makeComponent(obj);
    }

    public AbilityComponent makeAbility(JSONObject obj) {
        if(obj == null) throw new RuntimeException("Missing key: ability");
        return abilityCreators.get(obj.getString("id")).makeComponent(obj);
    }

    public GameObjectType makeGameObjectType(JSONObject obj) {
        return new GameObjectType(obj, this);
    }

    private interface ComponentCreator<ComponentType> {
        ComponentType makeComponent(JSONObject obj);
    }
}
