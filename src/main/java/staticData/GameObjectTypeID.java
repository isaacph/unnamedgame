package staticData;

import java.io.Serializable;
import java.util.UUID;

public class GameObjectTypeID implements Serializable {

    private final String nameID;

    public GameObjectTypeID(String nameID) {
        this.nameID = nameID;
    }

    public GameObjectTypeID(GameObjectTypeID other) {
        this.nameID = other.nameID;
    }

    @Override
    public int hashCode() {
        return nameID.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GameObjectTypeID && ((GameObjectTypeID) other).nameID.equals(nameID);
    }

    public String getName() {
        return nameID;
    }

    @Override
    public String toString() {
        return "GameObjectType:" + nameID.toString();
    }
}
