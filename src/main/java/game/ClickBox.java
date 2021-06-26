package game;

import model.GameObjectID;
import model.TeamID;
import org.joml.Vector2f;

public class ClickBox {

    public GameObjectID gameObjectID;
    public Vector2f min;
    public Vector2f max;
    public Vector2f depthOffset;
    public TeamID teamID;
    public boolean disabled;

    public ClickBox(GameObjectID id, TeamID teamID, Vector2f min, Vector2f max, Vector2f d) {
        this.gameObjectID = id;
        this.teamID = teamID;
        this.min = min;
        this.max = max;
        this.depthOffset = d;
        this.disabled = false;
    }

    public Vector2f center() {
        return new Vector2f((min.x + max.x) / 2.0f, (min.y + max.y) / 2.0f);
    }

    public Vector2f scale() {
        return new Vector2f(max.x - min.x, max.y - min.y);
    }

    public void set(ClickBox other) {
        this.gameObjectID = other.gameObjectID;
        this.min.set(other.min);
        this.max.set(other.max);
        this.depthOffset.set(other.depthOffset);
        this.disabled = other.disabled;
    }
}
