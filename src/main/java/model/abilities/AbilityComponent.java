package model.abilities;

import model.AbilityID;
import model.AbilityTypeID;
import org.json.JSONObject;

public interface AbilityComponent {

    int NO_SLOT = -1;

    AbilityTypeID getTypeID();
    AbilityID getID();
    JSONObject toJSON();
    int getSlot();

    /**
     * @return True if this is a passive ability that should be casted at the beginning of every turn
     */
    boolean isPassive();

    int getUsages();
    int getCooldown();
}
