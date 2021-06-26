package staticData;

import game.ActionArranger;
import org.json.JSONObject;

public interface AbilityComponent {

    AbilityID getID();
    JSONObject toJSON();
    int getSlot();
}
