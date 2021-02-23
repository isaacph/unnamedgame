import org.joml.Matrix4f;

public abstract class RenderComponent implements GameObjectComponent {

    public abstract void draw(Game game, Matrix4f orthoProj);
}
