import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class TextureComponent extends RenderComponent {

    private Texture texture;
    private Vector2f renderOffset;
    private Vector2f renderScale;
    private GameObject gameObject;

    public TextureComponent(GameObject gameObject, Texture texture, Vector2f renderOffset, Vector2f renderScale) {
        this.texture = texture;
        this.renderOffset = renderOffset;
        this.renderScale = renderScale;
        this.gameObject = gameObject;
    }

    @Override
    public void draw(Game game, Matrix4f orthoProj) {
        texture.bind();
        Vector2f pos = Camera.worldToViewSpace(new Vector2f(gameObject.x, gameObject.y));
        game.textureRenderer.draw(new Matrix4f(orthoProj)
            .translate(renderOffset.x + pos.x, renderOffset.y + pos.y, 0)
            .scale(renderScale.x, renderScale.y, 0),
            new Vector4f(1));
    }
}
