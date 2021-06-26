package game;

import org.joml.Vector2i;
import render.MoveAnimation;

public class MoveAnimator implements Animator {

    private MoveAction action;

    public MoveAnimator(MoveAction moveAction) {
        this.action = moveAction;
    }

    @Override
    public void animate(Game game) {
        if(game.world.gameObjects.get(action.objectID) == null) {
            throw new RuntimeException("Attempted to animate MoveAction on unknown game object:" + action.objectID);
        }
        game.animationManager.startAnimation(new MoveAnimation(game, action.objectID, new Vector2i(action.targetX, action.targetY)));
        action.execute(game.world, game.gameData);
    }
}
