package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameObject implements Serializable {

    public final GameObjectID uniqueID;
    public final int type;
    public int x, y;
    public TeamID team;
    public float health;

    public GameObject(GameObjectID uniqueID, int type, TeamID team, float health) {
        this.uniqueID = uniqueID;
        this.type = type;
        this.team = team;
        this.health = health;
    }
}
