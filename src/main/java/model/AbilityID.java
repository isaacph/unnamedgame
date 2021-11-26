package model;

import org.json.JSONObject;

import java.io.Serializable;

public class AbilityID implements Serializable {

    public final GameObjectTypeID gameObjectTypeID;
    public final AbilityTypeID abilityTypeID;
    public final int slot;

    public AbilityID(GameObjectTypeID goType, AbilityTypeID atID, int slot) {
        this.gameObjectTypeID = goType;
        this.abilityTypeID = atID;
        this.slot = slot;
        if(gameObjectTypeID == null) throw new RuntimeException("Cannot make ability ID from null game object type ID");
        if(abilityTypeID == null) throw new RuntimeException("Cannot make ability ID from null ability type ID");
    }

    public AbilityID(AbilityID other) {
        this(other.gameObjectTypeID, other.abilityTypeID, other.slot);
    }

    public boolean checkNull() {
        return gameObjectTypeID == null || abilityTypeID == null;
    }

    @Override
    public String toString() {
        return gameObjectTypeID.getName() + ":" + abilityTypeID.getName() + slot;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof AbilityID) {
            AbilityID obj = (AbilityID) other;
            if(obj.toString().equals(toString())) {
                return true;
            }
        }
        return false;
    }
}
