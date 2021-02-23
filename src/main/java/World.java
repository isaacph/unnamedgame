import org.joml.Matrix4f;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class World implements Serializable {

    public ArrayList<GameObject> gameObjects = new ArrayList<>();
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

    public void add(GameObject object) {
        for(GameObject obj : gameObjects) {
            if(obj.x == object.x && obj.y == object.y) {
                return;
            }
        }
        gameObjects.add(object);
    }
}
