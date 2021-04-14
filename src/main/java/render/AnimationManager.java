package render;

import game.Action;

import java.util.*;

public class AnimationManager {
    private final Collection<Animation> animations = new ArrayList<>();
    private boolean updating = false;
    private final Collection<Animation> toRemove = new ArrayList<>();
    private final Set<Integer> occupiedObjects = new HashSet<>();

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

    public boolean isObjectOccupied(int uniqueID) {
        return this.occupiedObjects.contains(uniqueID);
    }

    public void setObjectOccupied(int uniqueID, boolean occupied) {
        if(occupied) {
            this.occupiedObjects.add(uniqueID);
        } else {
            this.occupiedObjects.remove(uniqueID);
        }
    }
}
