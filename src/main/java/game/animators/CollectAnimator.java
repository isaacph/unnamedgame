package game.animators;

import game.ActionArranger;
import game.Animator;
import game.Game;
import model.Action;
import model.abilities.CollectAction;
import org.joml.Vector2i;

import java.util.Set;

public class CollectAnimator implements Animator {

    private CollectAction collectAction;

    public CollectAnimator(CollectAction collectAction) {
        this.collectAction = collectAction;
    }

    @Override
    public void animate(Game game) {
        if(game.world.gameObjects.get(collectAction.objectID) == null) {
            throw new RuntimeException("Attempted to animate CollectAction on unknown game object:" + collectAction.objectID);
        }
        // animate this later
        collectAction.execute(game.world, game.gameData);
    }
}
