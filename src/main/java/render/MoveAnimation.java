package render;

import game.*;
import model.GameObject;
import model.GameObjectID;
import model.World;
import org.joml.Vector2f;
import org.joml.Vector2i;
import model.GameData;

import java.util.ArrayList;
import java.util.List;

public class MoveAnimation implements Animation {

    public static final float TRAVEL_SPEED = 5.0f;

    private GameObjectID objectID;
    private Vector2i targetInt;
    private Vector2f target;
    private World world;
    private GameData gameData;
    private WorldRenderer renderer;
    private GameTime time;
    private AnimationManager animationManager;
    private ClickBoxManager clickBoxManager;
    private SelectGridManager selectGridManager;

    private Vector2f position;
    private List<Vector2i> path = new ArrayList<>();
    private int pathIndex;

    public MoveAnimation(Game res, GameObjectID objectID, Vector2i target) {
        this.world = res.world;
        this.clickBoxManager = res.clickBoxManager;
        this.renderer = res.worldRenderer;
        this.gameData = res.gameData;
        this.time = res.gameTime;
        this.animationManager = res.animationManager;
        this.selectGridManager = res.selectGridManager;

        this.objectID = objectID;
        this.targetInt = new Vector2i(target);
        this.target = new Vector2f(target.x, target.y);
    }

    @Override
    public void onStart() {
        GameObject object = world.gameObjects.get(objectID);
        this.position = new Vector2f(object.x, object.y);
        clickBoxManager.getGameObjectClickBox(objectID).disabled = true;
        this.animationManager.setObjectOccupied(objectID, true);
        this.path = Pathfinding.shortestPath(SelectGridManager.getWeightStorage(object.uniqueID, world, gameData), new Vector2i(object.x, object.y), this.targetInt, gameData.getType(object.type).getSpeed());
        this.pathIndex = 0;
    }

    @Override
    public void onUpdate() {
        if(this.pathIndex >= path.size()) {
            animationManager.endAction(this);
            return;
        }
        Vector2f move = new Vector2f(path.get(pathIndex).x, path.get(pathIndex).y).sub(position);
        float distSq  = move.lengthSquared();
        move.normalize((float) time.getDelta() * TRAVEL_SPEED);
        if(distSq <= 0.001 || distSq <= move.lengthSquared()) {
            this.position.set(path.get(pathIndex).x, path.get(pathIndex).y);
            renderer.getGameObjectRenderer(objectID).move(position);
            ++this.pathIndex;
        } else {
            this.position.add(move);
            renderer.getGameObjectRenderer(objectID).move(position);
        }
    }

    @Override
    public void onFinish() {
        renderer.getGameObjectRenderer(objectID).resetPosition();
        clickBoxManager.resetGameObjectClickBox(objectID);
        this.animationManager.setObjectOccupied(objectID, false);
    }

    @Override
    public void onObjectChange() {
        GameObject gameObject = world.gameObjects.get(objectID);
        if(gameObject == null) {
            animationManager.endAction(this);
        }
    }
}
