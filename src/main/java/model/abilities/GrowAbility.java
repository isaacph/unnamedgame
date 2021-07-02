package model.abilities;

import model.*;
import org.json.JSONObject;

public class GrowAbility implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("grow");

    private final int slot;
    private final GameObjectTypeID growInto;
    private final double cost;
    private final int requiredCount;

    private final GameObjectTypeID gameObjectTypeID;

    public GrowAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        GameObjectType.assertString(obj.getString("type"), getTypeID().getName());
        slot = obj.getInt("slot");
        growInto = new GameObjectTypeID(obj.getString("into"));
        cost = obj.getDouble("cost");
        requiredCount = obj.getInt("requiredCount");
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
        obj.put("into", growInto.getName());
        obj.put("cost", cost);
        obj.put("requiredCount", requiredCount);
        obj.put("slot", slot);
        return obj;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    public GameObjectTypeID getGrowInto() {
        return growInto;
    }

    public double getCost() {
        return cost;
    }

    public int getRequiredCount() {
        return requiredCount;
    }
}
