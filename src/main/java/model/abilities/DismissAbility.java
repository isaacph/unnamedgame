package model.abilities;

import model.*;
import org.json.JSONObject;

public class DismissAbility implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("dismiss");

    private final int slot;

    private GameObjectTypeID gameObjectTypeID;
    private double cost;

    public DismissAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        GameObjectType.assertString(obj.getString("type"), getTypeID().getName());
        slot = obj.getInt("slot");
        cost = obj.getDouble("cost");
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
        obj.put("cost", cost);
        obj.put("slot", slot);
        return obj;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    public double getCost() {
        return cost;
    }
}
