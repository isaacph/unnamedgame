package game;

import org.joml.Vector2f;
import render.WorldRenderer;
import staticData.GameData;
import staticData.GameObjectType;

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
    private final Map<GameObjectID, ClickBox> gameObjectClickBoxMap = new HashMap<>();

    public GameObjectID selectedID;

    public ClickBoxManager(World world, GameData gameData, Camera camera, WorldRenderer worldRenderer) {
        this.world = world;
        this.gameData = gameData;
        this.camera = camera;
        this.worldRenderer = worldRenderer;
        this.selectedID = null;
    }

    public void resetGameObjectCache() {
        clickBoxes.clear();
        gameObjectClickBoxMap.clear();
        for(GameObject obj : world.gameObjects.values()) {
            if(obj.alive) {
                ClickBox cb = makeClickBox(obj);
                clickBoxes.add(cb);
                gameObjectClickBoxMap.put(obj.uniqueID, cb);
            }
        }
    }

    public ClickBox makeClickBox(GameObject obj) {
        GameObjectType type = gameData.getType(obj.type);
        return type.makeClickBox(obj);
    }

    public ClickBox getGameObjectClickBox(GameObjectID uniqueID) {
        return gameObjectClickBoxMap.get(uniqueID);
    }

    public void resetGameObjectClickBox(GameObjectID uniqueID) {
        GameObject gameObject = world.gameObjects.get(uniqueID);
        ClickBox cb = gameObjectClickBoxMap.get(uniqueID);
        if(gameObject == null) {
            if(cb != null) {
                gameObjectClickBoxMap.remove(uniqueID);
                clickBoxes.remove(cb);
            }
            return;
        } else {
            if(cb != null) {
                if(gameObject.alive) {
                    cb.set(makeClickBox(gameObject));
                } else {
                    gameObjectClickBoxMap.remove(uniqueID);
                    clickBoxes.remove(cb);
                }
            } else if(gameObject.alive) {
                cb = makeClickBox(gameObject);
                clickBoxes.add(cb);
                gameObjectClickBoxMap.put(gameObject.uniqueID, cb);
            }
        }
    }

    public GameObject getGameObjectAtViewPosition(Vector2f viewPosition) {
        Vector2f position = new Vector2f(viewPosition);
        Vector2f topClickBoxDepthPosition = null;
        GameObject top = null;
        for(ClickBox clickBox : clickBoxes) {
            if(!clickBox.disabled && MathUtil.pointInside(
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

    public GameObject getGameObjectAtViewPosition(Vector2f viewPosition, TeamID team) {
        Vector2f position = new Vector2f(viewPosition);
        Vector2f topClickBoxDepthPosition = null;
        GameObject top = null;
        for(ClickBox clickBox : clickBoxes) {
            if(clickBox.teamID.equals(team) && !clickBox.disabled && MathUtil.pointInside(
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

    public GameObject getGameObjectAtViewPositionExcludeTeam(Vector2f viewPosition, TeamID team) {
        Vector2f position = new Vector2f(viewPosition);
        Vector2f topClickBoxDepthPosition = null;
        GameObject top = null;
        for(ClickBox clickBox : clickBoxes) {
            if(!clickBox.teamID.equals(team) && !clickBox.disabled && MathUtil.pointInside(
                position.x,
                position.y,
                clickBox.min.x,
                clickBox.min.y,
                clickBox.max.x,
                clickBox.max.y)) {
                if(topClickBoxDepthPosition == null || clickBox.depthOffset.y > topClickBoxDepthPosition.y) {
                    GameObject go = world.gameObjects.get(clickBox.gameObjectID);
                    if(go.alive) {
                        topClickBoxDepthPosition = clickBox.center().add(clickBox.depthOffset, new Vector2f());
                        top = world.gameObjects.get(clickBox.gameObjectID);
                    }
                }
            }
        }
        return top;
    }
}
