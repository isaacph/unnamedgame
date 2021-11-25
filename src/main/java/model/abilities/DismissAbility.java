package model.abilities;

import model.*;
import org.json.JSONObject;

public class DismissAbility extends AbilityImpl implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("dismiss");

    public DismissAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        super(obj, ID, objTypeID);
    }
}
