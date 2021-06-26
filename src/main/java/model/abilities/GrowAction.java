package model.abilities;

import model.*;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.ArrayList;
import java.util.Collection;

public class GrowAction implements Action {

    public ArrayList<GameObjectID> seeds;
    public GameObjectID newGameObjectResult = null;

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
        GrowAbility mainComp = gameData.getType(gameObjects.get(0).type).getAbility(GrowAbility.class);
        if(mainComp == null) return false;
        GameObjectTypeID growInto = mainComp.getGrowInto();
        for(GameObject gameObject : gameObjects) {
            if(!gameObject.alive || gameObject.team == null || !gameObject.team.equals(team)) return false;
            GrowAbility ability = gameData.getType(gameObject.type).getAbility(GrowAbility.class);
            if(ability == null) return false;
            if(!ability.getGrowInto().equals(growInto)) return false;
            square.add(new Vector2i(gameObject.x, gameObject.y));
        }
        if(!MathUtil.isSquare(square)) return false;
        return true;
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
        GrowAbility ability = gameData.getType(world.gameObjects.get(seeds.get(0)).type).getAbility(GrowAbility.class);
        GameObject newObj = world.gameObjectFactory.createGameObject(
                gameData.getType(ability.getGrowInto()),
                team);
        world.gameObjects.put(newObj.uniqueID, newObj);
        newObj.x = pos.x;
        newObj.y = pos.y;
        newObj.speedLeft = 0;
        newGameObjectResult = newObj.uniqueID;
    }

    @Override
    public AbilityID getID() {
        return GrowAbility.ID;
    }

}
