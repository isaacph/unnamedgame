package model.abilities;

import model.AbilityTypeID;
import model.Direct;
import model.GameObjectTypeID;
import org.joml.Vector2i;
import org.json.JSONArray;
import org.json.JSONObject;
import util.MathUtil;

import java.util.Collections;
import java.util.Set;

public class MoveAttackAbility extends AbilityImpl implements AbilityComponent {

    public static final AbilityTypeID ID = new AbilityTypeID("moveAttack");

    @Direct private double damage;
    @Direct private int range;
    @Direct private int speed;

    // whether this ability must be used on a visible target
    @Direct private boolean targeted;

    // whether this ability can be used targeting its user
    // still wouldn't damage user
    // only relevant if not targeted
    @Direct private boolean targetOnSelf;

    // a bunch of tiles that this attack hits at the same time
    // default direction is 1, 0
    // rotated based on chosen attack direction
    // use {(0, 0)} for no AOE
    private Set<Vector2i> aoe;

    public MoveAttackAbility(JSONObject obj, GameObjectTypeID objTypeID) {
        super(obj, ID, objTypeID);
        JSONArray aoeArray = obj.getJSONArray("aoe");
        aoe = MathUtil.jsonToTileSet(aoeArray);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = super.toJSON();
        obj.put("aoe", MathUtil.tileSetToJSON(aoe));
        return obj;
    }

    public double getDamage() { return damage; }

    public int getRange() {
        return range;
    }

    public int getSpeed() {
        return speed;
    }

    public Set<Vector2i> getAreaOfEffect() {
        return Collections.unmodifiableSet(aoe);
    }

    public boolean isTargeted() {
        return targeted;
    }

    public boolean canTargetOnSelf() {
        return targetOnSelf;
    }
}
