package game;

import org.joml.Vector2f;
import org.joml.Vector2i;
import render.AnimationManager;
import render.MoveAnimation;
import render.WorldRenderer;
import staticData.GameData;

public class MoveAction implements Action {

    private GameObjectID objectID;
    private int targetX, targetY;

    public MoveAction(GameObjectID objectID, int targetX, int targetY) {
        this.objectID = objectID;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    @Override
    public boolean validate(ClientID actor, World world, GameData gameData) {
        GameObject object = world.gameObjects.get(objectID);
        if(actor == null || object == null) {
            return false;
        }
        if(!object.team.equals(world.teams.getClientTeam(actor))) {
            return false;
        }
        if(object.x == targetX && object.y == targetY) {
            return false;
        }
        // at the moment we are only allowing move commands for 1x1 game objects, so that's all we consider here
        if(world.occupied(targetX, targetY, gameData) != null) {
            return false;
        }
        if(Pathfinding.shortestPath(SelectGridManager.getWeightStorage(objectID, world, gameData), new Vector2i(object.x, object.y), new Vector2i(targetX, targetY), gameData.getType(object.type).getBaseSpeed()).isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public void animate(GameResources resources) {
        if(resources.world.gameObjects.get(objectID) == null) {
            throw new RuntimeException("Attempted to animate MoveAction on unknown game object:" + objectID);
        }
        resources.animationManager.startAnimation(new MoveAnimation(resources, objectID, new Vector2i(targetX, targetY)));
        this.execute(resources.world, resources.gameData);
    }

    @Override
    public void execute(World world, GameData gameData) {
        if(world.gameObjects.get(objectID) == null) {
            throw new RuntimeException("Attempted to execute MoveAction on unknown game object:" + objectID);
        }
        world.gameObjects.get(objectID).x = targetX;
        world.gameObjects.get(objectID).y = targetY;
    }
}
