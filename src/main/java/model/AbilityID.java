package model;

import org.json.JSONObject;

import java.io.Serializable;

public class AbilityID implements Serializable {

    private final String nameID;

    public AbilityID(String nameID) {
        this.nameID = nameID;
    }

    public AbilityID(AbilityID other) {
        this.nameID = other.nameID;
    }

    public AbilityID(JSONObject obj) {
        this.nameID = obj.getString("nameID");
    }

    @Override
    public int hashCode() {
        return nameID.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AbilityID && ((AbilityID) other).nameID.equals(nameID);
    }

    public String getName() {
        return nameID;
    }

    @Override
    public String toString() {
        return "AbilityID:" + nameID.toString();
    }
}
