package model.abilities;

import model.*;
import org.json.JSONObject;

public abstract class AbilityImpl implements AbilityComponent {

    @Direct private AbilityTypeID type;
    @Direct private int slot;
    private GameObjectTypeID parentType;

    public AbilityImpl(JSONObject obj, AbilityTypeID abilityTypeID, GameObjectTypeID parentType) {
        GameObjectType.assertString(obj.getString("type"), abilityTypeID.getName());
        ReflectionJSON.extract(obj, this);
        this.parentType = parentType;
        this.type = abilityTypeID;
    }

    @Override
    public AbilityTypeID getTypeID() {
        return type;
    }

    @Override
    public AbilityID getID() {
        return new AbilityID(parentType, type, slot);
    }

    @Override
    public JSONObject toJSON() {
        return ReflectionJSON.makeJSON(this);
    }

    @Override
    public int getSlot() {
        return slot;
    }
}
