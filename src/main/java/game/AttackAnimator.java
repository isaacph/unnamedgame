package game;

import model.*;
import model.abilities.AttackAbility;
import model.abilities.AttackAction;
import org.joml.Vector2i;
import render.AttackAnimation;
import util.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AttackAnimator implements Animator {

    private AttackAction action;

    public AttackAnimator(AttackAction action) {
        this.action = action;
    }

    @Override
    public void animate(Game game) {
        GameObject attacker = game.world.gameObjects.get(action.attackerID);
        GameObject victim = game.world.gameObjects.get(action.victimID);
        AttackAbility attackerType = game.gameData.getType(attacker.type).getAbility(AttackAbility.class);
        boolean victimDead = victim.health <= attackerType.getDamage();
        game.animationManager.startAnimation(new AttackAnimation(action.attackerID, action.victimID, victimDead, game));
        action.execute(game.world, game.gameData);
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
