package model.abilities;

import model.AbilityComponent;
import model.AbilityID;
import model.GameObjectType;
import org.json.JSONObject;

public class AttackAbility implements AbilityComponent {

    public static final AbilityID ID = new AbilityID("attack");

    private final int slot;
    private final double damage;

    public AttackAbility(JSONObject obj) {
        GameObjectType.assertString(obj.getString("id"), getID().getName());
        slot = obj.getInt("slot");
        damage = obj.getDouble("damage");
    }

    @Override
    public AbilityID getID() {
        return ID;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", getID().getName());
        obj.put("damage", damage);
        return obj;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    public double getDamage() { return damage; }
}
