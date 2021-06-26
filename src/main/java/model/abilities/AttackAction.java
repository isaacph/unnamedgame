package model.abilities;

import model.*;
import org.joml.Vector2i;
import util.MathUtil;

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

}
