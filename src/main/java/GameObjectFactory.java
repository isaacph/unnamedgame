import org.joml.Vector2f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public class GameObjectFactory {

    private final Random uniqueIDGenerator = new Random();

    public GameObject createGameObject(GameObjectType type) {
        GameObject object = new GameObject(uniqueIDGenerator.nextLong(), type.uniqueID);
        type.initialize(object);
        return object;
    }
}
