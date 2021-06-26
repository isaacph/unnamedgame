package game;

import org.joml.Vector2i;
import staticData.GameData;
import staticData.SpawnAbility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SpawnAction implements Action {

    private GameObjectID sourceID;
    private int targetX, targetY;

    private GameObjectID cachedNewGameObject = null;

    public SpawnAction(GameObjectID sourceID, int targetX, int targetY) {
        this.sourceID = sourceID;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    @Override
    public boolean validate(ClientID actor, World world, GameData gameData) {
        GameObject source = world.gameObjects.get(sourceID);
        if(actor == null || source == null) return false;
        if(!source.alive) return false;
        if(gameData.getType(source.type).getAbility(SpawnAbility.class) == null) return false;
        if(world.teams.getClientTeam(actor) == null) return false;
        if(!source.team.equals(world.teams.getClientTeam(actor))) return false;
        if(world.occupied(targetX, targetY, gameData) != null) return false;
        if(world.getTileWeight(gameData, targetX, targetY) == Double.POSITIVE_INFINITY) return false;
        Set<Vector2i> adjacent = MathUtil.adjacentTiles(MathUtil.addToAll(gameData.getType(source.type).getRelativeOccupiedTiles(), new Vector2i(source.x, source.y)));
        if(!adjacent.contains(new Vector2i(targetX, targetY))) return false;
        if(source.speedLeft < 1) return false;
        return true;
    }

    @Override
    public void animate(Game gameResources) {
        this.execute(gameResources.world, gameResources.gameData);
        gameResources.worldRenderer.resetGameObjectRenderCache();
        gameResources.clickBoxManager.resetGameObjectClickBox(cachedNewGameObject);
    }

    @Override
    public void execute(World world, GameData gameData) {
        if(world.gameObjects.get(sourceID) == null) {
            throw new RuntimeException("Attempted to execute SpawnAction on unknown game object:" + sourceID);
        }
        GameObject object = world.gameObjects.get(sourceID);
        object.speedLeft -= 1;
        SpawnAbility ability = gameData.getType(object.type).getAbility(SpawnAbility.class);
        GameObject newGameObject = world.gameObjectFactory.createGameObject(
                gameData.getType(ability.getProducedType()),
                object.team);
        world.gameObjects.put(newGameObject.uniqueID, newGameObject);
        newGameObject.x = targetX;
        newGameObject.y = targetY;
        newGameObject.speedLeft = 0;
        cachedNewGameObject = newGameObject.uniqueID;
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
