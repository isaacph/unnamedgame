package model.abilities;

import model.*;

public class DismissAction implements Action {

    public AbilityID abilityID;
    public GameObjectID objectID;

    public DismissAction(AbilityID abilityID, GameObjectID objectID) {
        this.abilityID = abilityID;
        this.objectID = objectID;
    }

    @Override
    public boolean validate(ClientID actor, World world, GameData gameData) {
        if(abilityID == null || abilityID.checkNull() || objectID == null) return false;
        GameObject obj = world.gameObjects.get(objectID);
        if(obj == null) return false;
        DismissAbility ability = gameData.getAbility(DismissAbility.class, abilityID);
        if(ability == null) return false;
        if(!obj.alive) return false;
        if(!obj.team.equals(world.teams.getClientTeam(actor))) return false;
        if(obj.speedLeft < ability.getCost()) return false;
        return true;
    }

    @Override
    public void execute(World world, GameData gameData) {
        DismissAbility ability = gameData.getAbility(DismissAbility.class, abilityID);
        GameObject obj = world.gameObjects.get(objectID);
        obj.speedLeft -= ability.getCost();
        obj.alive = false;
    }

    @Override
    public AbilityTypeID getID() {
        return DismissAbility.ID;
    }
}
