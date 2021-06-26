package model;

import org.json.JSONObject;

public interface AbilityComponent {

    AbilityID getID();
    JSONObject toJSON();
    int getSlot();
}
