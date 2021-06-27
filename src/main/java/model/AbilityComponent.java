package model;

import org.json.JSONObject;

public interface AbilityComponent {

    AbilityTypeID getTypeID();
    AbilityID getID();
    JSONObject toJSON();
    int getSlot();
}
