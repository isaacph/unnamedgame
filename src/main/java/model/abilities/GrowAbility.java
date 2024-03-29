package model.abilities;

import model.*;
import org.json.JSONObject;

public class GrowAbility extends AbilityImpl implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("grow");

    @Direct private GameObjectTypeID into;
    @Direct private int requiredCount;

    public GrowAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        super(obj, ID, objTypeID);
    }

    public GameObjectTypeID getGrowInto() {
        return into;
    }

    public int getRequiredCount() {
        return requiredCount;
    }
}
