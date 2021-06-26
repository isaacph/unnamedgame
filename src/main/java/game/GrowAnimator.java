package game;

import model.*;
import model.abilities.GrowAbility;
import model.abilities.GrowAction;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.ArrayList;
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

        @Override
        public boolean arrange(Game game) {
            GameObject obj = game.world.gameObjects.get(game.clickBoxManager.selectedID);
            if(obj != null && !game.animationManager.isObjectOccupied(game.clickBoxManager.selectedID) && obj.alive) {
                boolean squareExists = false;
                for(Vector2i[] square : MathUtil.SQUARE_DIRECTIONS_DIAGONAL) {
                    boolean allPresent = true;
                    for(Vector2i tileOffset : square) {
                        Vector2i tile = new Vector2i(tileOffset).add(obj.x, obj.y);
                        GameObjectID id = game.world.occupied(tile.x, tile.y, game.gameData);
                        if(id == null) {
                            allPresent = false;
                            break;
                        }
                        GameObject tileObj = game.world.gameObjects.get(id);
                        GrowAbility ability = game.gameData.getType(tileObj.type).getAbility(GrowAbility.class);
                        if(ability == null) {
                            allPresent = false;
                            break;
                        }
                    }
                    if(allPresent) {
                        squareExists = true;
                    }
                }
                if(squareExists) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void clearArrangement(Game game) {
            game.worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>());
        }

        @Override
        public void changeMouseSelection(Game game, Set<Vector2i> occupied) {
            occupied.add(new Vector2i(game.mouseWorldPosition.x, game.mouseWorldPosition.y));
            occupied.add(new Vector2i(game.mouseWorldPosition.x + 1, game.mouseWorldPosition.y));
            occupied.add(new Vector2i(game.mouseWorldPosition.x + 1, game.mouseWorldPosition.y + 1));
            occupied.add(new Vector2i(game.mouseWorldPosition.x, game.mouseWorldPosition.y + 1));
        }

        @Override
        public Action createAction(Game game) {
            GameObject selectedObject = game.world.gameObjects.get(game.clickBoxManager.selectedID);
            if(selectedObject == null) return null;
            ArrayList<GameObjectID> seeds = new ArrayList<>();
            seeds.add(game.world.occupied(game.mouseWorldPosition.x, game.mouseWorldPosition.y, game.gameData));
            seeds.add(game.world.occupied(game.mouseWorldPosition.x + 1, game.mouseWorldPosition.y, game.gameData));
            seeds.add(game.world.occupied(game.mouseWorldPosition.x + 1, game.mouseWorldPosition.y + 1, game.gameData));
            seeds.add(game.world.occupied(game.mouseWorldPosition.x, game.mouseWorldPosition.y + 1, game.gameData));
            boolean valid = true;
            for(GameObjectID id : seeds) {
                if(id == null || game.animationManager.isObjectOccupied(id)) {
                    valid = false;
                    break;
                }
            }
            if(valid && seeds.contains(selectedObject.uniqueID)) {
                return new GrowAction(seeds);
            }
            return null;
        }
    }
}
