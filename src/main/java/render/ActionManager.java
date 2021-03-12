package render;

import java.util.ArrayList;
import java.util.Collection;

public class ActionManager {
    public Collection<Action> actions = new ArrayList<>();
    private boolean updating = false;
    private Collection<Action> toRemove = new ArrayList<>();

    public ActionManager() {

    }

    public void startAction(Action action) {
        actions.add(action);
        action.onStart();
    }

    public void update() {
        updating = true;
        for(Action action : actions) {
            action.onUpdate();
        }
        updating = false;
        for(Action action : toRemove) {
            actions.remove(action);
        }
        for(Action action : toRemove) {
            action.onFinish();
        }
        toRemove.clear();
    }

    public void endAction(Action action) {
        if(updating) {
            toRemove.add(action);
        } else {
            actions.remove(action);
            action.onFinish();
        }
    }
}
