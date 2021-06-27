package model;

import org.json.JSONObject;

import java.io.Serializable;

public class AbilityTypeID implements Serializable {

    private final String nameID;

    public AbilityTypeID(String nameID) {
        this.nameID = nameID;
    }

    public AbilityTypeID(AbilityTypeID other) {
        this.nameID = other.nameID;
    }

    public AbilityTypeID(JSONObject obj) {
        this.nameID = obj.getString("nameID");
    }

    @Override
    public int hashCode() {
        return nameID.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AbilityTypeID && ((AbilityTypeID) other).nameID.equals(nameID);
    }

    public String getName() {
        return nameID;
    }

    @Override
    public String toString() {
        return "AbilityTypeID:" + nameID.toString();
    }
}
