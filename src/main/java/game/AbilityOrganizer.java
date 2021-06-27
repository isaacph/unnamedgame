package game;

import model.*;
import model.abilities.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class AbilityOrganizer {

    public static Map<AbilityTypeID, Supplier<ActionArranger>> abilityActionArranger = getAbilityActionArrangers();
    public static Map<AbilityTypeID, AnimatorSupplier> abilityAnimatorSupplier = getAbilityAnimatorSuppliers();

    private static Map<AbilityTypeID, Supplier<ActionArranger>> getAbilityActionArrangers() {
        Map<AbilityTypeID, Supplier<ActionArranger>> map = new HashMap<>();
        map.put(MoveAbility.ID, MoveAnimator.Arranger::new);
        map.put(GrowAbility.ID, GrowAnimator.Arranger::new);
        map.put(AttackAbility.ID, AttackAnimator.Arranger::new);
        map.put(SpawnAbility.ID, SpawnAnimator.Arranger::new);
        return map;
    }

    private static Map<AbilityTypeID, AnimatorSupplier> getAbilityAnimatorSuppliers() {
        Map<AbilityTypeID, AnimatorSupplier> map = new HashMap<>();
        map.put(MoveAbility.ID, makeAnimatorSupplier(MoveAbility.ID, MoveAction.class, MoveAnimator::new));
        map.put(AttackAbility.ID, makeAnimatorSupplier(AttackAbility.ID, AttackAction.class, AttackAnimator::new));
        map.put(GrowAbility.ID, makeAnimatorSupplier(GrowAbility.ID, GrowAction.class, GrowAnimator::new));
        map.put(SpawnAbility.ID, makeAnimatorSupplier(SpawnAbility.ID, SpawnAction.class, SpawnAnimator::new));
        return map;
    }

    @SuppressWarnings("unchecked")
    private static <T> AnimatorSupplier makeAnimatorSupplier(AbilityTypeID id, Class<T> actionType, Function<T, Animator> simpleSupplier) {
        return action -> {
            if(action.getID().equals(id) && action.getClass().equals(actionType)) return simpleSupplier.apply((T) action);
            return null;
        };
    }
}
