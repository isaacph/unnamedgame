package game;

import org.joml.Vector2f;
import render.WorldRenderer;
import staticData.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClickBoxManager {

    private final World world;
    private final GameData gameData;
    private final Camera camera;
    private final WorldRenderer worldRenderer;

    private final Collection<ClickBox> clickBoxes = new ArrayList<>();
    private final Map<Integer, ClickBox> gameObjectClickBoxMap = new HashMap<>();

    public ClickBoxManager(World world, GameData gameData, Camera camera, WorldRenderer worldRenderer) {
        this.world = world;
        this.gameData = gameData;
        this.camera = camera;
        this.worldRenderer = worldRenderer;
    }

    public void resetGameObjectCache() {
        clickBoxes.clear();
        gameObjectClickBoxMap.clear();
        for(GameObject obj : world.gameObjects.values()) {
            ClickBox cb = makeClickBox(obj);
            clickBoxes.add(cb);
            gameObjectClickBoxMap.put(obj.uniqueID, cb);
        }
    }

    public ClickBox makeClickBox(GameObject obj) {
        return new ClickBox(obj.uniqueID,
            Camera.worldToViewSpace(new Vector2f(obj.x, obj.y))
                .add(gameData.getClickBoxOffset(obj.type))
                .sub(gameData.getClickBoxSize(obj.type).div(2)),
            Camera.worldToViewSpace(new Vector2f(obj.x, obj.y))
                .add(gameData.getClickBoxOffset(obj.type))
                .add(gameData.getClickBoxSize(obj.type).div(2)),
            gameData.getClickBoxDepthOffset(obj.type));
    }

    public ClickBox getGameObjectClickBox(int uniqueID) {
        return gameObjectClickBoxMap.get(uniqueID);
    }

    public GameObject getGameObjectAtViewPosition(Vector2f viewPosition) {
        Vector2f position = new Vector2f(viewPosition);
        Vector2f topClickBoxDepthPosition = null;
        GameObject top = null;
        for(ClickBox clickBox : clickBoxes) {
            if(!clickBox.disabled && Util.pointInside(
                position.x,
                position.y,
                clickBox.min.x,
                clickBox.min.y,
                clickBox.max.x,
                clickBox.max.y)) {
                if(topClickBoxDepthPosition == null || clickBox.depthOffset.y > topClickBoxDepthPosition.y) {
                    topClickBoxDepthPosition = clickBox.center().add(clickBox.depthOffset, new Vector2f());
                    top = world.gameObjects.get(clickBox.gameObjectID);
                }
            }
        }
        return top;
    }

    public static class ClickBox {
        public int gameObjectID;
        public Vector2f min;
        public Vector2f max;
        public Vector2f depthOffset;
        public boolean disabled;

        public ClickBox(int id, Vector2f min, Vector2f max, Vector2f d) {
            this.gameObjectID = id;
            this.min = min;
            this.max = max;
            this.depthOffset = d;
            this.disabled = false;
        }

        public Vector2f center() {
            return new Vector2f((min.x + max.x) / 2.0f, (min.y + max.y) / 2.0f);
        }

        public Vector2f scale() {
            return new Vector2f(max.x - min.x, max.y - min.y);
        }

        public void set(ClickBox other) {
            this.gameObjectID = other.gameObjectID;
            this.min.set(other.min);
            this.max.set(other.max);
            this.depthOffset.set(other.depthOffset);
            this.disabled = other.disabled;
        }
    }
}
