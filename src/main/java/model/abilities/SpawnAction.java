package model.abilities;

import model.*;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.*;

public class SpawnAction implements Action {

    public AbilityID abilityID;
    public GameObjectID sourceID;
    public int targetX, targetY;

    public GameObjectID newGameObjectResult = null;

    public SpawnAction(AbilityID abilityID, GameObjectID sourceID, int targetX, int targetY) {
        this.abilityID = abilityID;
        this.sourceID = sourceID;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    @Override
    public boolean validate(ClientID actor, World world, GameData gameData) {
        // get source object
        GameObject source = world.gameObjects.get(sourceID);
        if(actor == null || source == null) return false;
        if(!source.alive) return false;
        if(abilityID == null || abilityID.checkNull()) return false;

        // get ability
        SpawnAbility ability = gameData.getAbility(SpawnAbility.class, abilityID);
        if(ability == null) return false;
        GameObjectType prodType = gameData.getType(ability.getProducedType());
        if(prodType == null) return false;
        boolean restricted = ability.isRestricted();
        List<GameObjectTypeID> restrictedTypes = ability.getRestrictedObjects();

        // get team
        TeamID teamID = world.teams.getClientTeam(actor);
        if(teamID == null) return false;
        if(!source.team.equals(teamID)) return false;

        // check objects/tiles placing on top of
        // TODO: remove copying between here and SpawnAnimator
        boolean foundRequirement = false;
        for(Vector2i tile : prodType.getRelativeOccupiedTiles()) {
            Collection<GameObjectID> occupying = world.occupied(targetX + tile.x, targetY + tile.y, gameData);
            for(GameObjectID occID : occupying) {
                if(!restricted) {
                    return false;
                } else {
                    GameObject occObj = world.gameObjects.get(occID);
                    if(!restrictedTypes.contains(occObj.type)) {
                        return false;
                    } else {
                        foundRequirement = true;
                    }
                }
            }
            if(world.getPureTileWeight(gameData, targetX + tile.x, targetY + tile.y) == Double.POSITIVE_INFINITY) return false;
        }
        if(restricted && !foundRequirement) return false;
//        if(world.getTileWeight(gameData, targetX, targetY) == Double.POSITIVE_INFINITY) return false;

        // check shapes adjacent
        Set<Vector2i> adjacent = MathUtil.adjacentShapeOrigins(
                MathUtil.addToAll(gameData.getType(source.type).getRelativeOccupiedTiles(),
                        new Vector2i(source.x, source.y)),
                prodType.getRelativeOccupiedTiles());
        if(!adjacent.contains(new Vector2i(targetX, targetY))) return false;

        // check can afford
        if(source.speedLeft < ability.getSpeedCost()) return false;
        if(!Resource.canAfford(world.teams.getTeamResources(teamID), ability.getResourceCost())) return false;

        return true;
    }

    @Override
    public void execute(World world, GameData gameData) {
        if(world.gameObjects.get(sourceID) == null) {
            throw new RuntimeException("Attempted to execute SpawnAction on unknown game object:" + sourceID);
        }
        GameObject object = world.gameObjects.get(sourceID);
        SpawnAbility ability = gameData.getAbility(SpawnAbility.class, abilityID);
        object.speedLeft -= ability.getSpeedCost();
        GameObject newGameObject = world.gameObjectFactory.createGameObject(
                gameData.getType(ability.getProducedType()),
                object.team, gameData);
        world.gameObjects.put(newGameObject.uniqueID, newGameObject);
        newGameObject.x = targetX;
        newGameObject.y = targetY;
        newGameObject.speedLeft = 0;
        newGameObjectResult = newGameObject.uniqueID;

        // subtract resources
        TeamID teamID = object.team;
        Map<ResourceID, Integer> subtract = new HashMap<>(world.teams.getTeamResources(teamID));
        Resource.subtractResources(subtract, ability.getResourceCost());
        world.teams.setTeamResources(teamID, subtract);
    }

    @Override
    public AbilityTypeID getID() {
        return SpawnAbility.ID;
    }

}
