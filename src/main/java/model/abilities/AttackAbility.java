package model.abilities;

import model.*;
import org.json.JSONObject;

public class AttackAbility implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("attack");

    private final int slot;
    private final double damage;
    private final double cost;

    private final GameObjectTypeID gameObjectTypeID;

    public AttackAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        GameObjectType.assertString(obj.getString("type"), getTypeID().getName());
        slot = obj.getInt("slot");
        damage = obj.getDouble("damage");
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
        obj.put("damage", damage);
        obj.put("cost", cost);
        obj.put("slot", slot);
        return obj;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    public double getDamage() { return damage; }

    public double getCost() {
        return cost;
    }
}
