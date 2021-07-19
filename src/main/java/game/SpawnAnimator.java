package game;

import model.AbilityID;
import model.Action;
import model.GameObject;
import model.GameObjectType;
import model.abilities.SpawnAbility;
import model.abilities.SpawnAction;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SpawnAnimator implements Animator {

    public SpawnAction action;

    public SpawnAnimator(SpawnAction action) {
        this.action = action;
    }

    @Override
    public void animate(Game game) {
        action.execute(game.world, game.gameData);
        game.worldRenderer.resetGameObjectRenderCache();
        game.clickBoxManager.resetGameObjectClickBox(action.newGameObjectResult);
    }

    public static class Arranger implements ActionArranger {

        private AbilityID abilityID;

        @Override
        public boolean arrange(Game game, int slot) {
            GameObject obj = game.world.gameObjects.get(game.clickBoxManager.selectedID);
            if(obj == null) return false;
            GameObjectType type = game.gameData.getType(obj.type);
            if(type == null) return false;
            abilityID = new AbilityID(obj.type, SpawnAbility.ID, slot);
            SpawnAbility ability = game.gameData.getAbility(SpawnAbility.class, abilityID);
            if(ability != null && obj.speedLeft >= ability.getCost() && !game.animationManager.isObjectOccupied(game.clickBoxManager.selectedID) && obj.alive) {
                GameObjectType spawnedType = game.gameData.getType(ability.getProducedType());
                if(spawnedType == null) return false;

                Set<Vector2i> options = MathUtil.adjacentShapeOrigins(MathUtil.addToAll(game.gameData.getType(obj.type).getRelativeOccupiedTiles(), new Vector2i(obj.x, obj.y)),
                        game.gameData.getType(ability.getProducedType()).getRelativeOccupiedTiles());
                List<Vector2i> newOptions = new ArrayList<>();
                for(Vector2i option : options) {
                    boolean roomForNewObj = true;
                    Collection<Vector2i> shape = spawnedType.getRelativeOccupiedTiles();
                    for(Vector2i t : shape) {
                        Vector2i tile = new Vector2i(t).add(option);
                        if(!game.world.occupied(tile.x, tile.y, game.gameData).isEmpty() || game.world.getTileWeight(game.gameData, tile.x, tile.y) == Double.POSITIVE_INFINITY) {
                            roomForNewObj = false;
                        }
                    }
                    if(roomForNewObj) {
                        for(Vector2i t : shape) {
                            Vector2i tile = new Vector2i(t).add(option);
                            newOptions.add(tile);
                        }
                    }
                }
                game.selectGridManager.regenerateSelect(newOptions);
                game.worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>(game.selectGridManager.getSelectionGrid().map.values()));
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
            SpawnAbility ability = game.gameData.getAbility(SpawnAbility.class, abilityID);
            if(ability != null) {
                GameObjectType intoType = game.gameData.getType(ability.getProducedType());
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
            if(abilityID == null) throw new RuntimeException("Invalid: createAction() called before arrange() for an ability");
            GameObject selectedObject = game.world.gameObjects.get(game.clickBoxManager.selectedID);
            if(selectedObject != null && !game.animationManager.isObjectOccupied(selectedObject.uniqueID)) {
                return new SpawnAction(abilityID, game.clickBoxManager.selectedID, game.mouseWorldPosition.x, game.mouseWorldPosition.y);
            }
            return null;
        }
    }
}
