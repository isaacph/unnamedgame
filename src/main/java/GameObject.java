import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameObject implements Comparable<GameObject> {

    public int x, y;
    public GameObjectFactory.Type type;

    public RenderComponent renderComponent;

    public GameObject(GameObjectFactory.Type name) {
        this.type = name;
    }

    public void setRenderComponent(RenderComponent component) {
        this.renderComponent = component;
    }

    public void renderUpdate(Game game, double delta) {

    }

    public void draw(Game game, Matrix4f projView) {
        this.renderComponent.draw(game, projView);
    }

    public int compareTo(GameObject other) {
        return Float.compare(
            Camera.worldToViewSpace(new Vector2f(x, y)).y,
            Camera.worldToViewSpace(new Vector2f(other.x, other.y)).y
        );
    }
}
