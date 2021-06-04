package game;

import org.joml.Vector2i;
import render.AttackAnimation;
import render.MoveAnimation;
import staticData.GameData;
import staticData.GameObjectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AttackAction implements Action {

    private GameObjectID attackerID;
    private GameObjectID victimID;

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
        GameObjectType attackerType = gameData.getType(attacker.type);
        if(attackerType.getDamage() <= 0) return false;
        return true;
    }

    @Override
    public void animate(Game gameResources) {
        gameResources.animationManager.startAnimation(new AttackAnimation(attackerID, victimID, gameResources));
        this.execute(gameResources.world, gameResources.gameData);
    }

    @Override
    public void execute(World world, GameData gameData) {
        GameObject attacker = world.gameObjects.get(attackerID);
        GameObject victim = world.gameObjects.get(victimID);
        GameObjectType attackerType = gameData.getType(attacker.type);
        boolean victimDead = victim.health <= attackerType.getDamage();
        attacker.speedLeft -= 5;
        victim.health -= attackerType.getDamage();
        if(victimDead) {
            victim.alive = false;
            victim.health = 0;
        }
    }
}
