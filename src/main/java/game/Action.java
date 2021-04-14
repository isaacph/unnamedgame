package game;

import game.GameResources;

import java.io.Serializable;

public interface Action extends Serializable {

    /** Should call execute **/
    void animate(GameResources gameResources);


    void execute(World world);
}
