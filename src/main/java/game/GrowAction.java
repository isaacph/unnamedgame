package game;

import org.joml.Vector2i;
import staticData.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class GrowAction implements Action {

    public ArrayList<GameObjectID> seeds;
    public GameObjectID newGameObjectResult = null;

    public GrowAction(Collection<GameObjectID> seeds) {
        this.seeds = new ArrayList<>(seeds);
    }

    @Override
    public boolean validate(ClientID actor, World world, GameData gameData) {
        ArrayList<GameObject> gameObjects = new ArrayList<>();
        for(GameObjectID id : seeds) {
            GameObject go = world.gameObjects.get(id);
            gameObjects.add(go);
            if(go == null) return false;
        }
        if(gameObjects.size() != 4 || actor == null) return false;
        TeamID team = world.teams.getClientTeam(actor);
        if(team == null) return false;
        Collection<Vector2i> square = new ArrayList<>();
        GrowAbility mainComp = gameData.getType(gameObjects.get(0).type).getAbility(GrowAbility.class);
        if(mainComp == null) return false;
        GameObjectTypeID growInto = mainComp.getGrowInto();
        for(GameObject gameObject : gameObjects) {
            if(!gameObject.alive || gameObject.team == null || !gameObject.team.equals(team)) return false;
            GrowAbility ability = gameData.getType(gameObject.type).getAbility(GrowAbility.class);
            if(ability == null) return false;
            if(!ability.getGrowInto().equals(growInto)) return false;
            square.add(new Vector2i(gameObject.x, gameObject.y));
        }
        if(!MathUtil.isSquare(square)) return false;
        return true;
    }

    @Override
    public void execute(World world, GameData gameData) {
        TeamID team = world.gameObjects.get(seeds.get(0)).team;
        Collection<Vector2i> square = new ArrayList<>();
        for(GameObjectID id : seeds) {
            GameObject obj = world.gameObjects.get(id);
            obj.alive = false;
            square.add(new Vector2i(obj.x, obj.y));
        }
        Vector2i pos = MathUtil.squareTop(square);
        GrowAbility ability = gameData.getType(world.gameObjects.get(seeds.get(0)).type).getAbility(GrowAbility.class);
        GameObject newObj = world.gameObjectFactory.createGameObject(
                gameData.getType(ability.getGrowInto()),
                team);
        world.gameObjects.put(newObj.uniqueID, newObj);
        newObj.x = pos.x;
        newObj.y = pos.y;
        newObj.speedLeft = 0;
        newGameObjectResult = newObj.uniqueID;
    }

    @Override
    public AbilityID getID() {
        return GrowAbility.ID;
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
