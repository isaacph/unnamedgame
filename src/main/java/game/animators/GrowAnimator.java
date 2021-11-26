package game.animators;

import game.ActionArranger;
import game.Animator;
import game.Game;
import model.*;
import model.abilities.GrowAbility;
import model.abilities.GrowAction;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GrowAnimator implements Animator {

    private GrowAction action;

    public GrowAnimator(GrowAction action) {
        this.action = action;
    }

    @Override
    public void animate(Game game) {
        action.execute(game.world, game.gameData);
        game.worldRenderer.resetGameObjectRenderCache();
        game.animationManager.resetWhereNeeded();
        for(GameObjectID id : action.seeds) {
            game.clickBoxManager.resetGameObjectClickBox(id);
        }
        game.clickBoxManager.resetGameObjectClickBox(action.newGameObjectResult);
    }

    public static class Arranger implements ActionArranger {

        private AbilityID abilityID;

        @Override
        public boolean arrange(Game game, int slot) {
            GameObject obj = game.world.gameObjects.get(game.clickBoxManager.selectedID);
            GrowAbility ability = null;
            if(obj != null) {
                abilityID = new AbilityID(obj.type, GrowAbility.ID, slot);
                ability = game.gameData.getAbility(GrowAbility.class, abilityID);
            }
            if(ability != null && obj.speedLeft >= ability.getSpeedCost() &&
                    !game.animationManager.isObjectOccupied(game.clickBoxManager.selectedID) && obj.alive) {
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
            GrowAbility ability = game.gameData.getAbility(GrowAbility.class, abilityID);
            if(ability != null) {
                GameObjectType intoType = game.gameData.getType(ability.getGrowInto());
                if(intoType != null) {
                    Vector2i pos = game.mouseWorldPosition;
                    for(Vector2i offset : intoType.getRelativeOccupiedTiles()) {
                        occupied.add(new Vector2i(pos).add(offset));
                    }
                }
            }
        }

        @Override
        public Action createAction(Game game) {
            GameObject selectedObject = game.world.gameObjects.get(game.clickBoxManager.selectedID);
            if(selectedObject == null) return null;
            GrowAbility ability = game.gameData.getAbility(GrowAbility.class, abilityID);
            if(ability == null) return null;
            GameObjectTypeID intoID = ability.getGrowInto();
            GameObjectType intoType = game.gameData.getType(intoID);
            if(intoType == null) return null;
            Set<GameObjectID> seeds = new HashSet<>();
            for(Vector2i tileOffset : intoType.getRelativeOccupiedTiles()) {
                Vector2i tile = new Vector2i(game.mouseWorldPosition).add(tileOffset);
                Collection<GameObjectID> ids = game.world.occupied(tile.x, tile.y, game.gameData);
                for(GameObjectID id : ids) {
                    GameObject obj = game.world.gameObjects.get(id);
                    if(obj.type.equals(selectedObject.type)) {
                        seeds.add(id);
                    }
                }
            }
            if(!seeds.contains(selectedObject.uniqueID)) return null;
            return new GrowAction(abilityID, seeds, game.mouseWorldPosition.x, game.mouseWorldPosition.y);
        }
    }
}
