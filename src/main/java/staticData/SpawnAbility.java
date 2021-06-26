package staticData;

import org.json.JSONObject;

public class SpawnAbility implements AbilityComponent {

    public static final AbilityID ID = new AbilityID("spawn");

    private final int slot;
    private final GameObjectTypeID producedType;

    public SpawnAbility(JSONObject obj) {
        GameObjectType.assertString(obj.getString("id"), getID().getName());
        slot = obj.getInt("slot");
        producedType = new GameObjectTypeID(obj.getString("produce"));
    }

    @Override
    public AbilityID getID() {
        return ID;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", getID().getName());
        obj.put("produce", producedType.getName());
        return obj;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    public GameObjectTypeID getProducedType() {
        return producedType;
    }
}
