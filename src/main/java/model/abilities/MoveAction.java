package model.abilities;

import model.*;
import model.grid.Pathfinding;
import model.grid.TileGrid;
import org.joml.Vector2i;

import java.util.Collection;
import java.util.List;

public class MoveAction implements Action {

    public AbilityID abilityID;
    public GameObjectID objectID;
    public int targetX, targetY;

    public MoveAction(AbilityID abilityID, GameObjectID objectID, int targetX, int targetY) {
        this.abilityID = abilityID;
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
        GameObjectType type = gameData.getType(object.type);
        if(type == null) {
            return false;
        }
        if(abilityID == null || abilityID.checkNull()) return false;
        if(gameData.getAbility(MoveAbility.class, abilityID) == null) {
            return false;
        }
        if(!object.team.equals(world.teams.getClientTeam(actor))) {
            return false;
        }
        if(object.x == targetX && object.y == targetY) {
            return false;
        }
        Collection<Vector2i> shape = type.getRelativeOccupiedTiles();
        for(Vector2i offset : shape) {
            Collection<GameObjectID> obj = world.occupied(targetX + offset.x, targetY + offset.y, gameData);
            if(!obj.isEmpty() && (obj.size() != 1 || obj.iterator().next().equals(objectID))) {
                return false;
            }
        }
        List<Pathfinding.Movement> shortestPath = Pathfinding.shortestPath(
                new TileGrid(gameData, world),
                objectID,
                new Vector2i(object.x, object.y),
                new Vector2i(targetX, targetY),
                object.speedLeft);
        if(shortestPath.isEmpty()) {
            return false;
        }
        if(object.speedLeft < Pathfinding.getPathWeight(shortestPath)) {
            return false;
        }
        return true;
    }

    @Override
    public void execute(World world, GameData gameData) {
        if(world.gameObjects.get(objectID) == null) {
            throw new RuntimeException("Attempted to execute MoveAction on unknown game object:" + objectID);
        }
        GameObject object = world.gameObjects.get(objectID);
        List<Pathfinding.Movement> shortestPath = Pathfinding.shortestPath(
                new TileGrid(gameData, world),
                objectID,
                new Vector2i(object.x, object.y),
                new Vector2i(targetX, targetY), object.speedLeft);
        object.x = targetX;
        object.y = targetY;
        object.speedLeft -= Pathfinding.getPathWeight(shortestPath);
    }

    @Override
    public AbilityTypeID getID() {
        return MoveAbility.ID;
    }

}
