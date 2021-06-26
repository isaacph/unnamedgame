package game;

import model.Action;
import model.GameObject;
import model.abilities.MoveAbility;
import org.joml.Vector2i;
import render.MoveAnimation;
import model.abilities.MoveAction;

import java.util.ArrayList;
import java.util.Set;

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

    public static class Arranger implements ActionArranger {

        @Override
        public boolean arrange(Game game) {
            GameObject obj = game.world.gameObjects.get(game.clickBoxManager.selectedID);
            if(obj != null &&
                    obj.speedLeft > 0 &&
                    !game.animationManager.isObjectOccupied(game.clickBoxManager.selectedID) &&
                    obj.alive) {
                MoveAbility ability = game.gameData.getType(obj.type).getAbility(MoveAbility.class);
                if(ability == null) return false;
                game.selectGridManager.regenerateSelect(game.clickBoxManager.selectedID);
                game.worldRenderer.tileGridRenderer.buildSelect(
                        new ArrayList<>(game.selectGridManager.getSelectionGrid().map.values()));
                return true;
            }
            return false;
        }

        @Override
        public void clearArrangement(Game game) {
            game.worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>());
        }

        @Override
        public void changeMouseSelection(Game game, Set<Vector2i> occupied) {
        }

        @Override
        public Action createAction(Game game) {
            return new MoveAction(game.clickBoxManager.selectedID, game.mouseWorldPosition.x, game.mouseWorldPosition.y);
        }
    }
}
