package model.abilities;

import model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpawnAbility extends AbilityImpl implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("spawn");

    @Direct private GameObjectTypeID produce;
    @Direct.Optional private boolean restrict;
    private List<GameObjectTypeID> restrictObjects;

    public SpawnAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        super(obj, ID, objTypeID);
        this.restrictObjects = new ArrayList<>();
        if(this.restrict) {
            JSONArray arr = obj.getJSONArray("restrictObjects");
            for(int i = 0; i < arr.length(); ++i) {
                String line = arr.getString(i);
                restrictObjects.add(new GameObjectTypeID(line));
            }
        }
    }

    public GameObjectTypeID getProducedType() {
        return produce;
    }

    public boolean isRestricted() {
        return restrict;
    }

    public List<GameObjectTypeID> getRestrictedObjects() {
        return Collections.unmodifiableList(restrictObjects);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = super.toJSON();
        JSONArray arr = new JSONArray();
        for(GameObjectTypeID type : restrictObjects) {
            arr.put(type.getName());
        }
        if(!arr.isEmpty()) obj.put("restrictObjects", arr);
        return obj;
    }
}
