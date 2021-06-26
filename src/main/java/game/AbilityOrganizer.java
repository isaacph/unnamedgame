package game;

import staticData.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class AbilityOrganizer {

    public static Map<AbilityID, Supplier<ActionArranger>> abilityActionArranger = getAbilityActionArrangers();
    public static Map<AbilityID, AnimatorSupplier> abilityAnimatorSupplier = getAbilityAnimatorSuppliers();

    private static Map<AbilityID, Supplier<ActionArranger>> getAbilityActionArrangers() {
        Map<AbilityID, Supplier<ActionArranger>> map = new HashMap<>();
        map.put(MoveAbility.ID, MoveAction.Arranger::new);
        map.put(GrowAbility.ID, GrowAction.Arranger::new);
        map.put(AttackAbility.ID, AttackAction.Arranger::new);
        map.put(SpawnAbility.ID, SpawnAction.Arranger::new);
        return map;
    }

    private static Map<AbilityID, AnimatorSupplier> getAbilityAnimatorSuppliers() {
        Map<AbilityID, AnimatorSupplier> map = new HashMap<>();
        map.put(MoveAbility.ID, makeAnimatorSupplier(MoveAbility.ID, MoveAction.class, MoveAnimator::new));
        map.put(AttackAbility.ID, makeAnimatorSupplier(AttackAbility.ID, AttackAction.class, AttackAnimator::new));
        map.put(GrowAbility.ID, makeAnimatorSupplier(GrowAbility.ID, GrowAction.class, GrowAnimator::new));
        map.put(SpawnAbility.ID, makeAnimatorSupplier(SpawnAbility.ID, SpawnAction.class, SpawnAnimator::new));
        return map;
    }

    @SuppressWarnings("unchecked")
    private static <T> AnimatorSupplier makeAnimatorSupplier(AbilityID id, Class<T> actionType, Function<T, Animator> simpleSupplier) {
        return action -> {
            if(action.getID().equals(id) && action.getClass().equals(actionType)) return simpleSupplier.apply((T) action);
            return null;
        };
    }
}
