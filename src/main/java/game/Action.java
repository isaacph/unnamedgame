package game;

import staticData.AbilityID;
import staticData.GameData;

import java.io.Serializable;

public interface Action extends Serializable {

    boolean validate(ClientID actor, World world, GameData gameData);

    void execute(World world, GameData gameData);

    AbilityID getID();
}
