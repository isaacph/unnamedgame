package game;

import model.AbilityID;
import model.Action;
import model.GameObject;
import model.abilities.DismissAbility;
import model.abilities.DismissAction;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.ArrayList;
import java.util.Set;

public class DismissAnimator implements Animator {

    private DismissAction action;

    public DismissAnimator(DismissAction action) {
        this.action = action;
    }

    @Override
    public void animate(Game game) {
        action.execute(game.world, game.gameData);
        game.worldRenderer.resetGameObjectRenderCache();
        game.animationManager.resetWhereNeeded();
        game.clickBoxManager.resetGameObjectClickBox(action.objectID);
    }

    public static class Arranger implements ActionArranger {

        private AbilityID abilityID;

        @Override
        public boolean arrange(Game game, int slot) {
            GameObject obj = game.world.gameObjects.get(game.clickBoxManager.selectedID);
            DismissAbility ability = null;
            if(obj != null) {
                abilityID = new AbilityID(obj.type, DismissAbility.ID, slot);
                ability = game.gameData.getAbility(DismissAbility.class, abilityID);
            }
            if(ability != null && obj.speedLeft >= ability.getSpeedCost() &&
                    !game.animationManager.isObjectOccupied(game.clickBoxManager.selectedID) && obj.alive) {
                return true;
            }
            return false;
        }

        @Override
        public void clearArrangement(Game game) {
            game.worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>());
            game.clickBoxManager.selectedID = null;
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
            GameObject selectedObject = game.clickBoxManager.getGameObjectAtViewPosition(game.mouseViewPosition, game.world.teams.getClientTeam(game.clientInfo.clientID));
            if(selectedObject != null &&
                    selectedObject.team.equals(game.world.teams.getClientTeam(game.clientInfo.clientID)) &&
                    !game.animationManager.isObjectOccupied(selectedObject.uniqueID)
                    && selectedObject.uniqueID.equals(game.clickBoxManager.selectedID)) {
                return new DismissAction(abilityID, selectedObject.uniqueID);
            }
            return null;
        }
    }
}
