package game;

import render.AttackAnimation;
import staticData.AttackAbility;

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
}
