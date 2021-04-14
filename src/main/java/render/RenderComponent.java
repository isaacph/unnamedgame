package render;

import game.GameObjectComponent;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public abstract class RenderComponent implements GameObjectComponent, Comparable<RenderComponent>, IAnimatable {

    public abstract void draw(WorldRenderer renderer, Matrix4f projView);

    public abstract Vector2f getRenderPosition();

    public int compareTo(RenderComponent other) {
        return Float.compare(this.getRenderPosition().y, other.getRenderPosition().y);
    }
}
