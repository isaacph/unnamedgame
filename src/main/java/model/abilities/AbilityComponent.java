package model.abilities;

import model.AbilityID;
import model.AbilityTypeID;
import org.json.JSONObject;

public interface AbilityComponent {

    AbilityTypeID getTypeID();
    AbilityID getID();
    JSONObject toJSON();
    int getSlot();
}
