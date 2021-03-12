package game;

import org.joml.Vector2f;
import render.WorldRenderer;
import staticData.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class ClickBoxManager {

    private final World world;
    private final GameData gameData;
    private final Camera camera;
    private final WorldRenderer worldRenderer;

    private final Collection<ClickBox> clickBoxes = new ArrayList<>();

    public ClickBoxManager(World world, GameData gameData, Camera camera, WorldRenderer worldRenderer) {
        this.world = world;
        this.gameData = gameData;
        this.camera = camera;
        this.worldRenderer = worldRenderer;
    }

    public void resetGameObjectCache() {
        clickBoxes.clear();
        for(GameObject obj : world.gameObjects.values()) {
            clickBoxes.add(new ClickBox(obj.uniqueID,
                camera.viewToScreenSpace(Camera.worldToViewSpace(new Vector2f(obj.x, obj.y))
                    .add(gameData.getClickBoxOffset(obj.type))
                    .sub(gameData.getClickBoxSize(obj.type).div(2))),
                camera.viewToScreenSpace(Camera.worldToViewSpace(new Vector2f(obj.x, obj.y))
                    .add(gameData.getClickBoxOffset(obj.type))
                    .add(gameData.getClickBoxSize(obj.type).div(2))),
                camera.viewToScreenSpace(gameData.getClickBoxDepthOffset(obj.type))));
        }
    }

    public GameObject getGameObjectAtScreenPosition(Vector2f screenPosition) {
        Vector2f position = new Vector2f(screenPosition);
        Vector2f topClickBoxDepthPosition = null;
        GameObject top = null;
        for(ClickBox clickBox : clickBoxes) {
            if(Util.pointInside(
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

    private static class ClickBox {
        public int gameObjectID;
        public Vector2f min;
        public Vector2f max;
        public Vector2f depthOffset;

        public ClickBox(int id, Vector2f min, Vector2f max, Vector2f d) {
            this.gameObjectID = id;
            this.min = min;
            this.max = max;
            this.depthOffset = d;
        }

        public Vector2f center() {
            return new Vector2f(min.x + max.x / 2.0f, min.y + max.y / 2.0f);
        }
    }
}
