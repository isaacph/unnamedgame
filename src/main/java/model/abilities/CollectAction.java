package model.abilities;

import model.*;
import org.joml.Vector2f;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.*;

public class CollectAction implements Action {

    public AbilityID abilityID;
    public GameObjectID objectID;

    public CollectAction(AbilityID abilityID, GameObjectID objectID) {
        assert abilityID.abilityTypeID.equals(CollectAbility.ID);
        this.abilityID = abilityID;
        this.objectID = objectID;
    }

    @Override
    public boolean validate(ClientID actor, World world, GameData gameData) {

        GameObject gameObject = world.gameObjects.get(objectID);
        if(gameObject == null) return false;
        if(!gameObject.alive) return false;
        GameObjectType type = gameData.getType(gameObject.type);
        TeamID team = gameObject.team;
        if(team == null) return false;

        // get ability
        CollectAbility ability = gameData.getAbility(CollectAbility.class, abilityID);
        if(ability == null) return false;

        // check if on top of required object
        Set<GameObjectTypeID> collectFrom = ability.getCollectFrom();
        Set<Vector2i> occupyingTiles = MathUtil.addToAll(type.getRelativeOccupiedTiles(), new Vector2i(gameObject.x, gameObject.y));
        boolean hasCollectTarget = false;
        for(Vector2i tile : occupyingTiles) {
            Collection<GameObjectID> objsOnTile = world.occupied(tile.x, tile.y, gameData);
            for(GameObjectID objID : objsOnTile) {
                if(!objID.equals(objectID)) {
                    GameObject obj = world.gameObjects.get(objID);
                    if(collectFrom.contains(obj.type)) {
                        hasCollectTarget = true;
                    } else {
                        return false;
                    }
                }
            }
        }
        if(!hasCollectTarget) return false; // we don't believe the collector should be placed where it has nothing to collect

        return true;
    }

    @Override
    public void execute(World world, GameData gameData) {
        GameObject gameObject = world.gameObjects.get(objectID);
        GameObjectType type = gameData.getType(gameObject.type);
        TeamID team = gameObject.team;
        Map<ResourceID, Integer> currentAmount = new HashMap<>(world.teams.getTeamResources(team));
        Set<Vector2i> occupyingTiles = MathUtil.addToAll(type.getRelativeOccupiedTiles(), new Vector2i(gameObject.x, gameObject.y));
        for(Vector2i tile : occupyingTiles) {
            Collection<GameObjectID> objsOnTile = world.occupied(tile.x, tile.y, gameData);
            for(GameObjectID objID : objsOnTile) {
                if(!objID.equals(objectID)) {
                    GameObject obj = world.gameObjects.get(objID);
                    GameObjectType objType = gameData.getType(obj.type);
                    Map<ResourceID, Integer> addAmount = objType.getResources();
                    Resource.addResources(currentAmount, addAmount);
                }
            }
        }
        world.teams.setTeamResources(team, currentAmount);
    }

    @Override
    public AbilityTypeID getID() {
        return CollectAbility.ID;
    }
}
