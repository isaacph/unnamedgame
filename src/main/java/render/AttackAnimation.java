package render;

import game.Game;
import model.GameObjectID;
import org.joml.Vector2f;

public class AttackAnimation implements Animation {

    private GameObjectID attackerID;
    private GameObjectID victimID;
    private Game gameResources;

    private float progress = 0;
    private final float progressHit = 1.0f;
    private final float progressDone = 2.0f;

    private Vector2f attackerStart, attackerDirection;

    private boolean victimDead;

    public AttackAnimation(GameObjectID attackerID, GameObjectID victimID, boolean victimDead, Game gameResources) {
        this.attackerID = attackerID;
        this.victimID = victimID;
        this.gameResources = gameResources;
        this.victimDead = victimDead;
    }

    @Override
    public void onStart() {
        RenderComponent attackerRender = gameResources.worldRenderer.getGameObjectRenderer(attackerID);
        RenderComponent victimRender = gameResources.worldRenderer.getGameObjectRenderer(victimID);

        gameResources.animationManager.setObjectOccupied(attackerID, true);
        gameResources.animationManager.setObjectOccupied(victimID, true);
        gameResources.clickBoxManager.getGameObjectClickBox(attackerID).disabled = true;
        gameResources.clickBoxManager.getGameObjectClickBox(victimID).disabled = true;

        attackerStart = new Vector2f(attackerRender.getWorldCenter());
        attackerDirection = new Vector2f(victimRender.getWorldCenter()).sub(attackerStart);
        attackerDirection.normalize(attackerDirection.length() - 0.3f);

        if(victimDead) {
            victimRender.forceVisible(true);
        }
    }

    @Override
    public void onUpdate() {
        RenderComponent attackerRender = gameResources.worldRenderer.getGameObjectRenderer(attackerID);
        RenderComponent victimRender = gameResources.worldRenderer.getGameObjectRenderer(victimID);

        progress += gameResources.gameTime.getDelta();
        Vector2f attackerPosition;
        if(progress < progressHit) {
            attackerPosition = new Vector2f(attackerDirection).mul(progress / progressHit).add(attackerStart);
            attackerRender.move(attackerPosition);
        } else if(progress < progressDone) {
            attackerPosition = new Vector2f(attackerDirection).mul((progressDone - progress) / (progressDone - progressHit)).add(attackerStart);
            if(victimDead) {
                victimRender.forceVisible(false);
            }
            attackerRender.move(attackerPosition);
        } else {
            gameResources.animationManager.endAction(this);
        }
    }

    @Override
    public void onFinish() {
        RenderComponent attackerRender = gameResources.worldRenderer.getGameObjectRenderer(attackerID);
        RenderComponent victimRender = gameResources.worldRenderer.getGameObjectRenderer(victimID);

        if(victimDead) {
            victimRender.resetVisible();
        }
        attackerRender.resetPosition();
        gameResources.clickBoxManager.resetGameObjectClickBox(attackerID);
        gameResources.clickBoxManager.resetGameObjectClickBox(victimID);
        gameResources.animationManager.setObjectOccupied(attackerID, false);
        gameResources.animationManager.setObjectOccupied(victimID, false);
    }

    @Override
    public void onObjectChange() {
        if(gameResources.world.gameObjects.get(attackerID) == null || gameResources.world.gameObjects.get(victimID) == null) {
            gameResources.animationManager.endAction(this);
        }
    }
}
