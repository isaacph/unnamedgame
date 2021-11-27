package model.abilities;

import model.*;
import org.joml.Vector2i;
import util.MathUtil;

import java.util.Set;

public class AttackAction implements Action {

    public AbilityID abilityID;
    public GameObjectID attackerID;
    public GameObjectID victimID;

    public AttackAction(AbilityID abilityID, GameObjectID attackerID, GameObjectID victimID) {
        this.abilityID = abilityID;
        this.attackerID = attackerID;
        this.victimID = victimID;
    }

    @Override
    public boolean validate(ClientID actor, World world, GameData gameData) {
        if(abilityID == null || abilityID.checkNull()) return false;
        if(attackerID.equals(victimID)) return false;
        GameObject attacker = world.gameObjects.get(attackerID);
        GameObject victim = world.gameObjects.get(victimID);
        if(attacker == null || victim == null || actor == null) return false;
        if(!attacker.alive || !victim.alive) return false;
        GameObjectType attackerType = gameData.getType(attacker.type);
        GameObjectType victimType = gameData.getType(victim.type);
        if(attackerType == null || victimType == null) return false;
        AttackAbility ability = gameData.getAbility(AttackAbility.class, abilityID);
        if(ability == null) return false;
        Set<Vector2i> attackOptions = MathUtil.adjacentTilesDistance(
                MathUtil.addToAll(victimType.getRelativeOccupiedTiles(), new Vector2i(victim.x, victim.y)),
                ability.getRange()
        );
        boolean canAttack = false;
        for(Vector2i attackerShapeOffset : attackerType.getRelativeOccupiedTiles()) {
            Vector2i pos = new Vector2i(attackerShapeOffset).add(attacker.x, attacker.y);
            if(attackOptions.contains(pos)) {
                canAttack = true;
                break;
            }
        }
        if(!canAttack) return false;
        if(attacker.speedLeft < ability.getSpeedCost()) return false;
        if(ability.getDamage() <= 0) return false;
        return true;
    }

    @Override
    public void execute(World world, GameData gameData) {
        GameObject attacker = world.gameObjects.get(attackerID);
        GameObject victim = world.gameObjects.get(victimID);
        AttackAbility attackerType = gameData.getAbility(AttackAbility.class, abilityID);
        boolean victimDead = victim.health <= attackerType.getDamage();
        attacker.speedLeft -= attackerType.getSpeedCost();
        victim.health -= attackerType.getDamage();
        if(victimDead) {
            victim.alive = false;
            victim.health = 0;
        }
    }

    @Override
    public AbilityTypeID getID() {
        return AttackAbility.ID;
    }

}
