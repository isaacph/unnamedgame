package model.abilities;

import model.*;
import org.json.JSONObject;

public class SpawnAbility implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("spawn");

    private final int slot;
    private final GameObjectTypeID producedType;
    private final double cost;

    private final GameObjectTypeID gameObjectTypeID;

    public SpawnAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        GameObjectType.assertString(obj.getString("type"), getTypeID().getName());
        slot = obj.getInt("slot");
        producedType = new GameObjectTypeID(obj.getString("produce"));
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
        obj.put("produce", producedType.getName());
        obj.put("cost", cost);
        obj.put("slot", slot);
        return obj;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    public GameObjectTypeID getProducedType() {
        return producedType;
    }

    public double getCost() {
        return cost;
    }
}
