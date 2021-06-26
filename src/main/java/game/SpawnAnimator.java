package game;

import model.Action;
import model.GameObject;
import model.abilities.SpawnAbility;
import model.abilities.SpawnAction;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.ArrayList;
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

        @Override
        public boolean arrange(Game game) {
            GameObject obj = game.world.gameObjects.get(game.clickBoxManager.selectedID);
            if(obj == null) return false;
            SpawnAbility ability = game.gameData.getType(obj.type).getAbility(SpawnAbility.class);
            if(ability != null && obj.speedLeft > 0 && !game.animationManager.isObjectOccupied(game.clickBoxManager.selectedID) && obj.alive) {
                Set<Vector2i> options = MathUtil.adjacentTiles(MathUtil.addToAll(game.gameData.getType(obj.type).getRelativeOccupiedTiles(), new Vector2i(obj.x, obj.y)));
                List<Vector2i> newOptions = new ArrayList<>();
                for(Vector2i tile : options) {
                    if(game.world.occupied(tile.x, tile.y, game.gameData) == null && game.world.getTileWeight(game.gameData, tile.x, tile.y) < Double.POSITIVE_INFINITY) {
                        newOptions.add(tile);
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
        public void changeMouseSelection(Game game, Set<Vector2i> occupied) {}

        @Override
        public Action createAction(Game game) {
            GameObject selectedObject = game.world.gameObjects.get(game.clickBoxManager.selectedID);
            if(selectedObject != null && !game.animationManager.isObjectOccupied(selectedObject.uniqueID)) {
                return new SpawnAction(game.clickBoxManager.selectedID, game.mouseWorldPosition.x, game.mouseWorldPosition.y);
            }
            return null;
        }
    }
}
