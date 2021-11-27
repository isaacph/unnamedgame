package model.abilities;

import model.*;
import org.json.JSONObject;

public class AttackAbility extends AbilityImpl implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("attack");

    @Direct private double damage;
    @Direct private int range;

    public AttackAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        super(obj, ID, objTypeID);
    }

    public double getDamage() { return damage; }

    public int getRange() {
        return range;
    }
}
