package game;

import staticData.GameData;

import java.io.Serializable;

public interface Action extends Serializable {

    boolean validate(ClientID actor, World world, GameData gameData);

    /** Should call execute **/
    void animate(GameResources gameResources);


    void execute(World world, GameData gameData);
}
