package model.abilities;

import model.*;
import model.grid.Pathfinding;
import model.grid.TileGrid;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MoveAttackAction implements Action {

    public AbilityID abilityID;
    public GameObjectID objectID;
    public int moveTargetX, moveTargetY;

    // depending on whether this ability is targeted, uses targetID or uses target location (x,y)
    // if targeted, ignore target location (can be anything)
    public int targetX, targetY;
    public GameObjectID targetID;

    public MoveAttackAction(AbilityID abilityID, GameObjectID objectID, int moveTargetX, int moveTargetY, GameObjectID targetID, int targetX, int targetY) {
        assert abilityID.abilityTypeID.equals(getID());
        this.abilityID = abilityID;
        this.objectID = objectID;
        this.moveTargetX = moveTargetX;
        this.moveTargetY = moveTargetY;
        this.targetID = targetID;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    @Override
    public boolean validate(ClientID actor, World world, GameData gameData) {
        // check abilityID
        if(abilityID == null || abilityID.checkNull()) {
            return false;
        }
        if(!abilityID.abilityTypeID.equals(getID())) {
            return false;
        }

        // check user gameObject
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
        if(!object.team.equals(world.teams.getClientTeam(actor))) {
            return false;
        }
        // TODO: temp
        if(object.speedLeft < 1) {
            return false;
        }

        // check gameObject type
        GameObjectType type = gameData.getType(object.type);
        if(type == null) {
            return false;
        }

        // get ability-specific information
        MoveAttackAbility ability = gameData.getAbility(MoveAttackAbility.class, abilityID);
        if(ability == null) {
            return false;
        }

        // can we move
        // can we attack
        //   if targeted, is the target in range
        //   else
        //      if !targetOnSelf, is the target not on self
        //      is the target location in range

        // check if targetID exists for targeted abilities

        // see if we can move to the target location
        List<Pathfinding.Movement> shortestPath = Pathfinding.shortestPath(
                new TileGrid(gameData, world),
                objectID,
                new Vector2i(object.x, object.y),
                new Vector2i(moveTargetX, moveTargetY),
                ability.getSpeed());
        if(shortestPath.isEmpty()) {
            return false;
        }
        if(ability.getSpeed() < Pathfinding.getPathWeight(shortestPath)) {
            return false;
        }

        // checks if each tile occupied by this unit in the target location is not occupied by non-self units
        Set<Vector2i> destShape = MathUtil.addToAll(type.getRelativeOccupiedTiles(), new Vector2i(targetX, targetY));
        Set<Vector2i> attackOptions = MathUtil.adjacentTilesDistance(destShape, ability.getRange());
        for(Vector2i pos : destShape) {
            Collection<GameObjectID> obj = world.occupied(pos.x, pos.y, gameData);
            if(!obj.isEmpty() && (obj.size() != 1 || obj.iterator().next().equals(objectID))) {
                return false;
            }
        }

        if(ability.isTargeted()) {
            // find target
            if(targetID == null) {
                return false;
            }
            GameObject target = world.gameObjects.get(targetID);
            if(target == null) {
                return false;
            }
            GameObjectType targetType = gameData.getType(target.type);
            if(targetType == null) {
                return false;
            }
            // is the target in range
            Set<Vector2i> optionsOnTarget = MathUtil.addToAll(targetType.getRelativeOccupiedTiles(), new Vector2i(target.x, target.y));
            optionsOnTarget.retainAll(attackOptions);
            if(optionsOnTarget.isEmpty()) {
                return false;
            }
        } else {
            if(!ability.canTargetOnSelf()) {
                // check if target location is on self is on self
                if(destShape.contains(new Vector2i(targetX, targetY))) {
                    return false;
                }
            }

            // check if target location is in range
            if(!attackOptions.contains(new Vector2i(targetX, targetY))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void execute(World world, GameData gameData) {
        if(world.gameObjects.get(objectID) == null) {
            throw new RuntimeException("Attempted to execute MoveAction on unknown game object:" + objectID);
        }
        GameObject object = world.gameObjects.get(objectID);
//        List<Pathfinding.Movement> shortestPath = Pathfinding.shortestPath(
//                new TileGrid(gameData, world),
//                objectID,
//                new Vector2i(object.x, object.y),
//                new Vector2i(moveTargetX, moveTargetY), object.speedLeft);
        object.x = moveTargetX;
        object.y = moveTargetY;
//        object.speedLeft -= Pathfinding.getPathWeight(shortestPath);
        MoveAttackAbility ability = gameData.getAbility(MoveAttackAbility.class, abilityID);
        Vector2i targetLocation = new Vector2i(targetX, targetY);
        if(ability.isTargeted()) {
            GameObject target = world.gameObjects.get(targetID);
            targetLocation.set(target.x, target.y);
        }
        Set<Vector2i> aoe = MathUtil.addToAll(ability.getAreaOfEffect(), targetLocation);
        Set<GameObjectID> affected = new HashSet<>();
        for(Vector2i pos : aoe) {
            affected.addAll(world.occupied(pos.x, pos.y, gameData));
        }
        for(GameObjectID id : affected) {
            GameObject affectedObj = world.gameObjects.get(id);
            affectedObj.health -= ability.getDamage();
        }
        // TODO: temp
        object.speedLeft -= 1;
    }

    @Override
    public AbilityTypeID getID() {
        return MoveAttackAbility.ID;
    }
}
