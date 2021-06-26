package staticData;

import org.json.JSONObject;

public class GrowAbility implements AbilityComponent {

    public static final AbilityID ID = new AbilityID("grow");

    private final int slot;
    private final GameObjectTypeID growInto;

    public GrowAbility(JSONObject obj) {
        GameObjectType.assertString(obj.getString("id"), getID().getName());
        slot = obj.getInt("slot");
        growInto = new GameObjectTypeID(obj.getString("into"));
    }

    @Override
    public AbilityID getID() {
        return ID;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", getID().getName());
        obj.put("into", growInto.getName());
        return obj;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    public GameObjectTypeID getGrowInto() {
        return growInto;
    }
}
