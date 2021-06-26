package model.abilities;

import model.*;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.Set;

public class SpawnAction implements Action {

    public GameObjectID sourceID;
    public int targetX, targetY;

    public GameObjectID newGameObjectResult = null;

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
        newGameObjectResult = newGameObject.uniqueID;
    }

    @Override
    public AbilityID getID() {
        return SpawnAbility.ID;
    }

}
