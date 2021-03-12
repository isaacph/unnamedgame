package render;

import game.GameTime;
import game.World;
import org.joml.Vector2f;
import org.joml.Vector2i;
import staticData.GameData;

public class MoveAction implements Action {

    private int objectID;
    private Vector2f target;
    private World world;
    private GameData gameData;
    private WorldRenderer renderer;
    private GameTime time;
    private ActionManager actionManager;

    private Vector2f position;

    public MoveAction(GameTime time, ActionManager actionManager, World world, WorldRenderer renderer, GameData gameData, int objectID, Vector2i target) {
        this.world = world;
        this.renderer = renderer;
        this.gameData = gameData;
        this.objectID = objectID;
        this.target = new Vector2f(target.x, target.y);
        this.time = time;
        this.actionManager = actionManager;
    }

    @Override
    public void onStart() {
        this.position = new Vector2f(world.gameObjects.get(objectID).x, world.gameObjects.get(objectID).y);
    }

    @Override
    public void onUpdate() {
        Vector2f move = new Vector2f(target).sub(position);
        float distSq  = move.lengthSquared();
        move.normalize((float) time.getDelta());
        if(distSq <= move.lengthSquared()) {
            actionManager.endAction(this);
        } else {
            this.position.add(move);
            renderer.getGameObjectRenderer(objectID).move(position);
        }
    }

    @Override
    public void onFinish() {
        renderer.getGameObjectRenderer(objectID).resetPosition();
    }
}
