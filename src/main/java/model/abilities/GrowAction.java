package model.abilities;

import model.*;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class GrowAction implements Action {

    public AbilityID abilityID;
    public int growX, growY;
    public ArrayList<GameObjectID> seeds;
    public GameObjectID newGameObjectResult = null;

    public GrowAction(AbilityID abilityID, Collection<GameObjectID> seeds, int posX, int posY) {
        this.abilityID = abilityID;
        this.seeds = new ArrayList<>(seeds);
        this.growX = posX;
        this.growY = posY;
    }

    @Override
    public boolean validate(ClientID actor, World world, GameData gameData) {
        if(actor == null) return false;
        TeamID growerTeamID = world.teams.getClientTeam(actor);
        ArrayList<GameObject> gameObjects = new ArrayList<>();
        for(GameObjectID id : seeds) {
            GameObject go = world.gameObjects.get(id);
            if(gameObjects.contains(go)) return false;
            gameObjects.add(go);
            if(go == null) return false;
        }
        if(gameObjects.isEmpty()) return false;
        if(abilityID == null || abilityID.checkNull()) return false;

        GameObjectTypeID growerTypeID = abilityID.gameObjectTypeID;
        GrowAbility ability = gameData.getAbility(GrowAbility.class, abilityID);
        if(ability == null) return false;
        if(gameObjects.size() != ability.getRequiredCount()) return false;
        for(GameObject go : gameObjects) {
            if(!go.type.equals(growerTypeID)) return false;
            if(go.speedLeft < ability.getCost()) return false;
            if(!go.team.equals(growerTeamID)) return false;
            if(!go.alive) return false;
        }
        GameObjectTypeID growIntoID = ability.getGrowInto();
        GameObjectType growInto = gameData.getType(growIntoID);
        if(growInto == null) return false;
        boolean[] goFound = new boolean[seeds.size()];
        int numFound = 0;
        Set<Vector2i> tilesToOccupy = MathUtil.addToAll(growInto.getRelativeOccupiedTiles(), new Vector2i(growX, growY));
        for(Vector2i tile : tilesToOccupy) {
            if(world.getPureTileWeight(gameData, tile.x, tile.y) == Double.POSITIVE_INFINITY) {
                return false;
            }
            Collection<GameObjectID> occupiers = world.occupied(tile.x, tile.y, gameData);
            for(GameObjectID occupier : occupiers) {
                int index = seeds.indexOf(occupier);
                if(index == -1) return false;
                if(!goFound[index]) {
                    goFound[index] = true;
                    ++numFound;
                    break;
                }
            }
        }
        if(numFound != seeds.size()) return false;
        return true;
    }

    @Override
    public void execute(World world, GameData gameData) {
        TeamID team = world.gameObjects.get(seeds.get(0)).team;
        for(GameObjectID id : seeds) {
            GameObject obj = world.gameObjects.get(id);
            GrowAbility specificAbility = gameData.getAbility(GrowAbility.class, abilityID);
            obj.speedLeft -= specificAbility.getCost();
            obj.alive = false;
        }
        Vector2i pos = new Vector2i(growX, growY);
        GrowAbility ability = gameData.getAbility(GrowAbility.class, abilityID);
        GameObject newObj = world.gameObjectFactory.createGameObject(
                gameData.getType(ability.getGrowInto()),
                team, gameData);
        world.gameObjects.put(newObj.uniqueID, newObj);
        newObj.x = pos.x;
        newObj.y = pos.y;
        newObj.speedLeft = 0;
        newGameObjectResult = newObj.uniqueID;
    }

    @Override
    public AbilityTypeID getID() {
        return GrowAbility.ID;
    }

}
