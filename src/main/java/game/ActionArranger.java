package game;

import org.joml.Vector2i;

import java.util.Set;

public interface ActionArranger {

    boolean arrange(Game game);

    void clearArrangement(Game game);

    void changeMouseSelection(Game game, Set<Vector2i> occupied);

    Action createAction(Game gameResources);
}
