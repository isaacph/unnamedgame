package model.abilities;

import game.*;
import model.*;
import org.joml.Vector2i;

import java.util.List;

public class MoveAction implements Action {

    public GameObjectID objectID;
    public int targetX, targetY;

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
        if(!object.alive) {
            return false;
        }
        if(world.teams.getClientTeam(actor) == null) {
            return false;
        }
        if(!gameData.getType(object.type).hasAbility(MoveAbility.ID)) {
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
        Pathfinding.WeightStorage ws = SelectGridManager.getWeightStorage(objectID, world, gameData);
        List<Vector2i> shortestPath = Pathfinding.shortestPath(ws, new Vector2i(object.x, object.y), new Vector2i(targetX, targetY), object.speedLeft);
        if(shortestPath.isEmpty()) {
            return false;
        }
        if(object.speedLeft < Pathfinding.getPathWeight(shortestPath, ws)) {
            return false;
        }
        return true;
    }

    @Override
    public void execute(World world, GameData gameData) {
        if(world.gameObjects.get(objectID) == null) {
            throw new RuntimeException("Attempted to execute MoveAction on unknown game object:" + objectID);
        }
        Pathfinding.WeightStorage ws = SelectGridManager.getWeightStorage(objectID, world, gameData);
        GameObject object = world.gameObjects.get(objectID);
        List<Vector2i> shortestPath = Pathfinding.shortestPath(ws, new Vector2i(object.x, object.y), new Vector2i(targetX, targetY), object.speedLeft);
        object.x = targetX;
        object.y = targetY;
        object.speedLeft -= Pathfinding.getPathWeight(shortestPath, ws);
    }

    @Override
    public AbilityID getID() {
        return MoveAbility.ID;
    }

}
