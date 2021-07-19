package model.abilities;

import model.*;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.Set;

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
        GameObject source = world.gameObjects.get(sourceID);
        if(abilityID == null || abilityID.checkNull()) return false;
        if(actor == null || source == null) return false;
        if(!source.alive) return false;
        SpawnAbility ability = gameData.getAbility(SpawnAbility.class, abilityID);
        if(ability == null) return false;
        GameObjectType prodType = gameData.getType(ability.getProducedType());
        if(prodType == null) return false;
        if(world.teams.getClientTeam(actor) == null) return false;
        if(!source.team.equals(world.teams.getClientTeam(actor))) return false;
        for(Vector2i tile : prodType.getRelativeOccupiedTiles()) {
            if(!world.occupied(targetX + tile.x, targetY + tile.y, gameData).isEmpty()) return false;
            if(world.getPureTileWeight(gameData, targetX + tile.x, targetY + tile.y) == Double.POSITIVE_INFINITY) return false;
        }
        if(world.getTileWeight(gameData, targetX, targetY) == Double.POSITIVE_INFINITY) return false;
        Set<Vector2i> adjacent = MathUtil.adjacentShapeOrigins(
                MathUtil.addToAll(gameData.getType(source.type).getRelativeOccupiedTiles(),
                        new Vector2i(source.x, source.y)),
                prodType.getRelativeOccupiedTiles());
        if(!adjacent.contains(new Vector2i(targetX, targetY))) return false;
        if(source.speedLeft < ability.getCost()) return false;
        return true;
    }

    @Override
    public void execute(World world, GameData gameData) {
        if(world.gameObjects.get(sourceID) == null) {
            throw new RuntimeException("Attempted to execute SpawnAction on unknown game object:" + sourceID);
        }
        GameObject object = world.gameObjects.get(sourceID);
        SpawnAbility ability = gameData.getAbility(SpawnAbility.class, abilityID);
        object.speedLeft -= ability.getCost();
        GameObject newGameObject = world.gameObjectFactory.createGameObject(
                gameData.getType(ability.getProducedType()),
                object.team, gameData);
        world.gameObjects.put(newGameObject.uniqueID, newGameObject);
        newGameObject.x = targetX;
        newGameObject.y = targetY;
        newGameObject.speedLeft = 0;
        newGameObjectResult = newGameObject.uniqueID;
    }

    @Override
    public AbilityTypeID getID() {
        return SpawnAbility.ID;
    }

}
