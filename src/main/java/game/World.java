package game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class World implements Serializable {

    public Map<Integer, GameObject> gameObjects = new HashMap<>();
    public Grid.Group grid = new Grid.Group();

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

    public boolean add(GameObject object) {
        for(GameObject obj : gameObjects.values()) {
            if(obj.x == object.x && obj.y == object.y) {
                return false;
            }
        }
        gameObjects.put(object.uniqueID, object);
        return true;
    }


}
