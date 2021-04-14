package render;

import game.ClickBoxManager;
import game.GameResources;
import game.GameTime;
import game.World;
import org.joml.Vector2f;
import org.joml.Vector2i;
import staticData.GameData;

public class MoveAnimation implements Animation {

    private int objectID;
    private Vector2i targetInt;
    private Vector2f target;
    private World world;
    private GameData gameData;
    private WorldRenderer renderer;
    private GameTime time;
    private AnimationManager animationManager;
    private ClickBoxManager clickBoxManager;

    private Vector2f position;

    public MoveAnimation(GameResources res, int objectID, Vector2i target) {
        this.world = res.world;
        this.clickBoxManager = res.clickBoxManager;
        this.renderer = res.worldRenderer;
        this.gameData = res.gameData;
        this.time = res.gameTime;
        this.animationManager = res.animationManager;

        this.objectID = objectID;
        this.targetInt = new Vector2i(target);
        this.target = new Vector2f(target.x, target.y);
    }

    @Override
    public void onStart() {
        this.position = new Vector2f(world.gameObjects.get(objectID).x, world.gameObjects.get(objectID).y);
        clickBoxManager.getGameObjectClickBox(objectID).disabled = true;
        this.animationManager.setObjectOccupied(objectID, true);
    }

    @Override
    public void onUpdate() {
        Vector2f move = new Vector2f(target).sub(position);
        float distSq  = move.lengthSquared();
        move.normalize((float) time.getDelta());
        if(distSq <= move.lengthSquared()) {
            animationManager.endAction(this);
        } else {
            this.position.add(move);
            renderer.getGameObjectRenderer(objectID).move(position);
        }
    }

    @Override
    public void onFinish() {
        renderer.getGameObjectRenderer(objectID).resetPosition();
        clickBoxManager.getGameObjectClickBox(objectID).set(clickBoxManager.makeClickBox(world.gameObjects.get(objectID)));
        this.animationManager.setObjectOccupied(objectID, false);
    }
}
