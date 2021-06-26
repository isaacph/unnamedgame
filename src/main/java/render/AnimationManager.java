package render;

import model.GameObjectID;

import java.util.*;

public class AnimationManager {
    private final Collection<Animation> animations = new ArrayList<>();
    private boolean updating = false;
    private final Collection<Animation> toRemove = new ArrayList<>();
    private final Map<GameObjectID, Integer> occupiedObjects = new HashMap<>();

    public AnimationManager() {

    }

    public void reset() {
        if(updating) {
            throw new RuntimeException("Attempted to reset animations during an animation!");
        }
        animations.clear();
        toRemove.clear();
        occupiedObjects.clear();
    }

    public void resetWhereNeeded() {
        for(Animation animation : animations) {
            animation.onObjectChange();
        }
    }

    public void startAnimation(Animation animation) {
        animations.add(animation);
        animation.onStart();
    }

    public void update() {
        updating = true;
        for(Animation animation : animations) {
            animation.onUpdate();
        }
        updating = false;
        for(Animation animation : toRemove) {
            animations.remove(animation);
        }
        for(Animation animation : toRemove) {
            animation.onFinish();
        }
        toRemove.clear();
    }

    public void endAction(Animation animation) {
        if(updating) {
            toRemove.add(animation);
        } else {
            animation.onFinish();
            animations.remove(animation);
        }
    }

    public boolean isObjectOccupied(GameObjectID uniqueID) {
        return this.occupiedObjects.get(uniqueID) != null && this.occupiedObjects.get(uniqueID) > 0;
    }

    public void setObjectOccupied(GameObjectID uniqueID, boolean occupied) {
        Integer occupation = occupiedObjects.get(uniqueID);
        if(occupation == null) occupation = 0;
        if(occupied) {
            occupation++;
        } else {
            occupation = Math.max(0, occupation - 1);
        }
        this.occupiedObjects.put(uniqueID, occupation);
    }
}
