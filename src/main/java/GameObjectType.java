import org.joml.Vector2f;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GameObjectType implements Serializable {

    public String name;
    public String texture;
    public Vector2f textureOffset;
    public Vector2f textureScale;
    public int uniqueID;

    public GameObjectType(int uniqueID, String name, String texture, Vector2f offset, Vector2f scale) {
        this.name = name;
        this.texture = texture;
        this.textureOffset = offset;
        this.textureScale = scale;
        this.uniqueID = uniqueID;
    }

    public void initialize(GameObject gameObject) {

    }
}
