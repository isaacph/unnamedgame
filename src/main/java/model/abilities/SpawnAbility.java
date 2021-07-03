package model.abilities;

import model.*;
import org.json.JSONObject;

public class SpawnAbility extends AbilityImpl implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("spawn");

    @Direct private GameObjectTypeID produce;
    @Direct private double cost;

    public SpawnAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        super(obj, ID, objTypeID);
    }

    public GameObjectTypeID getProducedType() {
        return produce;
    }

    public double getCost() {
        return cost;
    }
}
