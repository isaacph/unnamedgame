package render;

import org.joml.Matrix4f;
import org.joml.Vector2f;

public abstract class RenderComponent implements Comparable<RenderComponent>, IAnimatable, IDisappearable {

    public void drawGround(WorldRenderer renderer, Matrix4f projView) {}

    public abstract void draw(WorldRenderer renderer, Matrix4f projView);

    public abstract Vector2f getScreenRenderPosition();
    public abstract Vector2f getWorldCenter();
    public abstract Vector2f getCenterOffset();

    public int compareTo(RenderComponent other) {
        return Float.compare(this.getScreenRenderPosition().y, other.getScreenRenderPosition().y);
    }
}
