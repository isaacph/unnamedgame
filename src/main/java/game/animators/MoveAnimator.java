package game.animators;

import game.ActionArranger;
import game.Animator;
import game.Game;
import model.AbilityID;
import model.Action;
import model.GameObject;
import model.GameObjectType;
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

        private AbilityID abilityID;

        @Override
        public boolean arrange(Game game, int slot) {
            GameObject obj = game.world.gameObjects.get(game.clickBoxManager.selectedID);
            if(obj != null &&
                    obj.speedLeft > 0 &&
                    !game.animationManager.isObjectOccupied(game.clickBoxManager.selectedID) &&
                    obj.alive) {
                abilityID = new AbilityID(obj.type, MoveAbility.ID, slot);
                MoveAbility ability = game.gameData.getAbility(MoveAbility.class, abilityID);
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
            GameObjectType type = game.gameData.getType(game.world.gameObjects.get(game.clickBoxManager.selectedID).type);
            Vector2i pos = game.mouseWorldPosition;
            for(Vector2i offset : type.getRelativeOccupiedTiles()) {
                occupied.add(new Vector2i(pos).add(offset));
            }
        }

        @Override
        public Action createAction(Game game) {
            if(abilityID == null) throw new RuntimeException("Invalid: createAction() called before arrange() for an ability");
            return new MoveAction(abilityID, game.clickBoxManager.selectedID, game.mouseWorldPosition.x, game.mouseWorldPosition.y);
        }
    }
}
