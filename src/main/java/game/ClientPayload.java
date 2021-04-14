package game;

import game.GameResources;

import java.io.Serializable;

public interface ClientPayload extends Serializable {

    void execute(GameResources gameResources);
}
