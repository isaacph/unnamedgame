package model.abilities;

import model.*;
import org.json.JSONObject;

public class DismissAbility extends AbilityImpl implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("dismiss");

    @Direct private double cost;

    public DismissAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        super(obj, ID, objTypeID);
    }

    public double getCost() {
        return cost;
    }
}
