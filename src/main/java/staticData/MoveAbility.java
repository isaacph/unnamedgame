package staticData;

import game.*;
import org.json.JSONObject;

public class MoveAbility implements AbilityComponent {

    public static final AbilityID ID = new AbilityID("move");

    private final int slot;

    public MoveAbility(JSONObject obj) {
        GameObjectType.assertString(obj.getString("id"), getID().getName());
        slot = obj.getInt("slot");
    }

    @Override
    public AbilityID getID() {
        return ID;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", getID().getName());
        return obj;
    }

    @Override
    public int getSlot() {
        return slot;
    }
}
