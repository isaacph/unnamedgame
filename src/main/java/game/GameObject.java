package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameObject implements Serializable {

    public final int uniqueID;
    public final int type;
    public int x, y;

    public GameObject(int uniqueID, int type) {
        this.uniqueID = uniqueID;
        this.type = type;
    }
}
