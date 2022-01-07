package model;

import java.util.HashMap;
import java.util.Map;

public final class LifeCycle {

    private LifeCycle() {}

    public interface AbilityHook {
        void hook(Action action);
    }

    public interface GameObjectHook {
        void hook(GameObject gameObject);
    }

    public static class Hooks {
        public final Map<AbilityID, AbilityHook> preActionHooks = new HashMap<>();
        public final Map<AbilityID, AbilityHook> postActionHooks = new HashMap<>();
        public final Map<TeamID, Runnable> prePassivesHook = new HashMap<>();
        public final Map<TeamID, Runnable> postPassivesHook = new HashMap<>();
        public GameObjectHook preSpawnHook;
        public GameObjectHook postSpawnHook;

        public Hooks() {}
    }

    public static void useAllPassives(World world, GameData gameData, Hooks hooks, TeamID team) {

    }

    public static void endTurn(World world, GameData gameData, Hooks hooks, TeamID turn) {
        // force "turn"'s turn to end
    }

    public static void endClientTurn(World world, GameData gameData, Hooks hooks, ClientID clientID) {
        // end the clients turn, end the teams turn if all clients are ended
    }

    public static void useAbility(World world, GameData gameData, Hooks hooks, TeamID teamID, Action action) {

    }


}
