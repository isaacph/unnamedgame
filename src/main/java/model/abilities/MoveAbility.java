package model.abilities;

import model.*;
import org.json.JSONObject;

public class MoveAbility implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("move");

    private final int slot;

    private GameObjectTypeID gameObjectTypeID;

    public MoveAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        GameObjectType.assertString(obj.getString("type"), getTypeID().getName());
        slot = obj.getInt("slot");
        gameObjectTypeID = objTypeID;
    }

    @Override
    public AbilityTypeID getTypeID() {
        return ID;
    }

    @Override
    public AbilityID getID() {
        return new AbilityID(gameObjectTypeID, ID, slot);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("type", getTypeID().getName());
        obj.put("slot", slot);
        return obj;
    }

    @Override
    public int getSlot() {
        return slot;
    }
}
