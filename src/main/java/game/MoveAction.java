package game;

import org.joml.Vector2i;
import render.MoveAnimation;
import staticData.AbilityID;
import staticData.GameData;
import staticData.MoveAbility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public static class Arranger implements ActionArranger {

        @Override
        public boolean arrange(Game game) {
            GameObject obj = game.world.gameObjects.get(game.clickBoxManager.selectedID);
            if(obj != null &&
                    obj.speedLeft > 0 &&
                    !game.animationManager.isObjectOccupied(game.clickBoxManager.selectedID) &&
                    obj.alive) {
                MoveAbility ability = game.gameData.getType(obj.type).getAbility(MoveAbility.class);
                if(ability == null) return false;
                game.selectGridManager.regenerateSelect(game.clickBoxManager.selectedID);
                game.worldRenderer.tileGridRenderer.buildSelect(
                        new ArrayList<>(game.selectGridManager.getSelectionGrid().map.values()));
                return true;
            }
            return false;
        }

        @Override
        public void clearArrangement(Game game) {
            game.worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>());
        }

        @Override
        public void changeMouseSelection(Game game, Set<Vector2i> occupied) {
        }

        @Override
        public Action createAction(Game game) {
            return new MoveAction(game.clickBoxManager.selectedID, game.mouseWorldPosition.x, game.mouseWorldPosition.y);
        }
    }
}
