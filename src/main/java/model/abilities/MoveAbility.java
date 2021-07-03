package model.abilities;

import model.*;
import org.json.JSONObject;

public class MoveAbility implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("move");

    @Direct public int slot;
    @Direct public GameObjectTypeID type;

    public MoveAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        GameObjectType.assertString(obj.getString("type"), getTypeID().getName());
        ReflectionJSON.extract(obj, this);
    }

    @Override
    public AbilityTypeID getTypeID() {
        return ID;
    }

    @Override
    public AbilityID getID() {
        return new AbilityID(type, ID, slot);
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
