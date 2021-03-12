package staticData;

import game.GameObject;
import org.joml.Vector2f;

import java.io.Serializable;

public class GameObjectType implements Serializable {

    public String name;
    public String texture;
    public Vector2f textureOffset;
    public Vector2f textureScale;
    public Vector2f clickBoxOffset;
    public Vector2f clickBoxSize;
    public Vector2f clickBoxDepthOffset;
    public int uniqueID;

    public GameObjectType(int uniqueID, String name, String texture, Vector2f offset, Vector2f scale, Vector2f cbOffset, Vector2f cbSize, Vector2f cbdOffset) {
        this.name = name;
        this.texture = texture;
        this.textureOffset = offset;
        this.textureScale = scale;
        this.uniqueID = uniqueID;
        this.clickBoxOffset = cbOffset;
        this.clickBoxSize = cbSize;
        this.clickBoxDepthOffset = cbdOffset;
    }

    public void initialize(GameObject gameObject) {

    }
}
