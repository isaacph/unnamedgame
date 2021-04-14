package game;

import org.joml.Vector2f;
import org.joml.Vector2i;
import render.AnimationManager;
import render.MoveAnimation;
import render.WorldRenderer;
import staticData.GameData;

public class MoveAction implements Action {

    private int objectID = -1;
    private int targetX = 0, targetY = 0;

    public MoveAction() {
    }

    public MoveAction(int objectID, int targetX, int targetY) {
        this.objectID = objectID;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    @Override
    public void animate(GameResources resources) {
        if(resources.world.gameObjects.get(objectID) == null) {
            throw new RuntimeException("Attempted to animate MoveAction on unknown game object:" + objectID);
        }
        resources.animationManager.startAnimation(new MoveAnimation(resources, objectID, new Vector2i(targetX, targetY)));
        this.execute(resources.world);
    }

    @Override
    public void execute(World world) {
        if(world.gameObjects.get(objectID) == null) {
            throw new RuntimeException("Attempted to execute MoveAction on unknown game object:" + objectID);
        }
        world.gameObjects.get(objectID).x = targetX;
        world.gameObjects.get(objectID).y = targetY;
    }
}