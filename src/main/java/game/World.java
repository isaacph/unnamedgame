package game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class World implements Serializable {

    public Map<Integer, GameObject> gameObjects = new HashMap<>();
    public Grid.Group grid = new Grid.Group();
    private int version = 0;

    public World() {

    }

//    public void renderUpdate(Game game, double delta) {
//        for(GameObject obj : gameObjects) {
//            obj.renderUpdate(game, delta);
//        }
//    }

//    public void draw(Game game, Matrix4f projView) {
//
//        for(GameObject obj : gameObjects) {
//            obj.draw(game, projView);
//        }
//    }

    public boolean occupied(int x, int y) {
        for(GameObject obj : gameObjects.values()) {
            if(obj.x == x && obj.y == y) {
                return true;
            }
        }
        return false;
    }

    public boolean add(GameObject object) {
        if(occupied(object.x, object.y)) {
            return false;
        }
        gameObjects.put(object.uniqueID, object);
        return true;
    }

    public int incrementVersion() {
        return ++version;
    }

    public int getVersion() {
        return version;
    }

    public void setWorld(World other) {
        gameObjects = other.gameObjects;
        grid = other.grid;
        version = other.version;
    }
}
