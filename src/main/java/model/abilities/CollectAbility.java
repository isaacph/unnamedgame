package model.abilities;

import model.AbilityTypeID;
import model.Direct;
import model.GameObjectTypeID;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class CollectAbility extends AbilityImpl implements AbilityComponent {
    public static final AbilityTypeID ID = new AbilityTypeID("collect");

    private Set<GameObjectTypeID> collectFrom;

    public CollectAbility(JSONObject obj, GameObjectTypeID parentType) {
        super(obj, ID, parentType);

        collectFrom = new HashSet<>();
        JSONArray arr = obj.getJSONArray("collectFrom");
        for(int i = 0; i < arr.length(); ++i) {
            String next = arr.getString(i);
            collectFrom.add(new GameObjectTypeID(next));
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = super.toJSON();
        JSONArray arr = new JSONArray();
        for(GameObjectTypeID id : collectFrom) {
            arr.put(id.getName());
        }
        obj.put("collectFrom", arr);
        return obj;
    }

    /**
     * @return An unmodifiable set of the game object type ids this can collect from
     */
    public Set<GameObjectTypeID> getCollectFrom() {
        return Collections.unmodifiableSet(collectFrom);
    }

    @Override
    public boolean isPassive() {
        return true;
    }
}
