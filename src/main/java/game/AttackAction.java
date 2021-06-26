package game;

import org.joml.Vector2i;
import render.AttackAnimation;
import render.MoveAnimation;
import staticData.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AttackAction implements Action {

    public GameObjectID attackerID;
    public GameObjectID victimID;

    public AttackAction(GameObjectID attackerID, GameObjectID victimID) {
        this.attackerID = attackerID;
        this.victimID = victimID;
    }

    @Override
    public boolean validate(ClientID actor, World world, GameData gameData) {
        if(attackerID.equals(victimID)) return false;
        GameObject attacker = world.gameObjects.get(attackerID);
        GameObject victim = world.gameObjects.get(victimID);
        if(attacker == null || victim == null || actor == null) return false;
        if(!attacker.alive || !victim.alive) return false;
        Set<Vector2i> options = MathUtil.adjacentTiles(MathUtil.addToAll(gameData.getType(victim.type).getRelativeOccupiedTiles(), new Vector2i(victim.x, victim.y)));
        if(!options.contains(new Vector2i(attacker.x, attacker.y))) return false;
        if(attacker.speedLeft < 5) return false;
        AttackAbility attackerType = gameData.getType(attacker.type).getAbility(AttackAbility.class);
        if(attackerType.getDamage() <= 0) return false;
        return true;
    }

    @Override
    public void execute(World world, GameData gameData) {
        GameObject attacker = world.gameObjects.get(attackerID);
        GameObject victim = world.gameObjects.get(victimID);
        AttackAbility attackerType = gameData.getType(attacker.type).getAbility(AttackAbility.class);
        boolean victimDead = victim.health <= attackerType.getDamage();
        attacker.speedLeft -= 5;
        victim.health -= attackerType.getDamage();
        if(victimDead) {
            victim.alive = false;
            victim.health = 0;
        }
    }

    @Override
    public AbilityID getID() {
        return AttackAbility.ID;
    }

    public static class Arranger implements ActionArranger {

        @Override
        public boolean arrange(Game game) {
            GameObject obj = game.world.gameObjects.get(game.clickBoxManager.selectedID);
            if(obj != null && obj.speedLeft >= 5 && !game.animationManager.isObjectOccupied(game.clickBoxManager.selectedID) && obj.alive) {
                AttackAbility ability = game.gameData.getType(obj.type).getAbility(AttackAbility.class);
                if(ability == null) return false;
                Set<Vector2i> options = MathUtil.adjacentTiles(MathUtil.addToAll(game.gameData.getType(obj.type).getRelativeOccupiedTiles(), new Vector2i(obj.x, obj.y)));
                List<Vector2i> newOptions = new ArrayList<>();
                for(Vector2i tile : options) {
                    GameObjectID victimID = game.world.occupied(tile.x, tile.y, game.gameData);
                    if(victimID != null) {
                        GameObject victim = game.world.gameObjects.get(victimID);
                        if(victim.alive && (victim.team == null || !victim.team.equals(obj.team))) {
                            newOptions.add(tile);
                        }
                    }
                }
                game.selectGridManager.regenerateSelect(newOptions);
                game.worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>(game.selectGridManager.getSelectionGrid().map.values()));
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
            GameObject targetObject = game.clickBoxManager.getGameObjectAtViewPositionExcludeTeam(game.mouseViewPosition, game.world.teams.getClientTeam(game.clientInfo.clientID));
            if(targetObject != null && !targetObject.team.equals(game.world.teams.getClientTeam(game.clientInfo.clientID))) {
                occupied.addAll(MathUtil.addToAll(game.gameData.getType(targetObject.type).getRelativeOccupiedTiles(), new Vector2i(targetObject.x, targetObject.y)));
            }
        }

        @Override
        public Action createAction(Game game) {
            GameObject selectedObject = game.clickBoxManager.getGameObjectAtViewPositionExcludeTeam(game.mouseViewPosition, game.world.teams.getClientTeam(game.clientInfo.clientID));
            if(selectedObject != null && !selectedObject.team.equals(game.world.teams.getClientTeam(game.clientInfo.clientID)) && !game.animationManager.isObjectOccupied(selectedObject.uniqueID)) {
                return new AttackAction(game.clickBoxManager.selectedID, selectedObject.uniqueID);
            }
            return null;
        }
    }
}
