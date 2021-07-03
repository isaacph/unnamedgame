package model.abilities;

import model.*;
import org.json.JSONObject;

public class MoveAbility extends AbilityImpl implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("move");

    public MoveAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        super(obj, ID, objTypeID);
    }
}
