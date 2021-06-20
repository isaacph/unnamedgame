package game;

import org.joml.Vector2i;
import staticData.GameData;
import staticData.GameObjectTypeID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GrowAction implements Action {

    public ArrayList<GameObjectID> seeds;
    private GameObjectID newGameObjectCache = null;

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
        GameObjectTypeID growInto = gameData.getType(gameObjects.get(0).type).producedType();
        for(GameObject gameObject : gameObjects) {
            if(!gameObject.alive || gameObject.team == null || !gameObject.team.equals(team)) return false;
            if(!gameData.getType(gameObject.type).canGrow()) return false;
            if(!gameData.getType(gameObject.type).producedType().equals(growInto)) return false;
            square.add(new Vector2i(gameObject.x, gameObject.y));
        }
        if(!MathUtil.isSquare(square)) return false;
        return true;
    }

    @Override
    public void animate(Game gameResources) {
        execute(gameResources.world, gameResources.gameData);
        gameResources.worldRenderer.resetGameObjectRenderCache();
        gameResources.animationManager.resetWhereNeeded();
        for(GameObjectID id : seeds) {
            gameResources.clickBoxManager.resetGameObjectClickBox(id);
        }
        gameResources.clickBoxManager.resetGameObjectClickBox(newGameObjectCache);
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
        GameObject newObj = world.gameObjectFactory.createGameObject(
                gameData.getType(
                        gameData.getType(world.gameObjects.get(seeds.get(0)).type)
                        .producedType()),
                team);
        world.gameObjects.put(newObj.uniqueID, newObj);
        newObj.x = pos.x;
        newObj.y = pos.y;
        newObj.speedLeft = 0;
        newGameObjectCache = newObj.uniqueID;
    }
}
