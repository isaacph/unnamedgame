import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class TextureComponent extends RenderComponent {

    private Texture texture;
    private Vector2f renderOffset;
    private Vector2f renderScale;
    private GameObject gameObject;

    public TextureComponent(GameData gameData, GameObject gameObject, WorldRenderer.GameObjectTextures textureLibrary) {
        GameObjectType type = gameData.getType(gameObject.type);
        this.texture = textureLibrary.getTexture(type.texture);
        this.renderOffset = type.textureOffset;
        this.renderScale = type.textureScale;
        this.gameObject = gameObject;
    }

    @Override
    public void draw(WorldRenderer game, Matrix4f orthoProj) {
        texture.bind();
        Vector2f pos = Camera.worldToViewSpace(new Vector2f(gameObject.x, gameObject.y));
        game.textureRenderer.draw(new Matrix4f(orthoProj)
            .translate(renderOffset.x + pos.x, renderOffset.y + pos.y, 0)
            .scale(renderScale.x, renderScale.y, 0),
            new Vector4f(1));
    }

    public Vector2f getRenderPosition() {
        return Camera.worldToViewSpace(new Vector2f(this.gameObject.x + 0.5f, this.gameObject.y + 0.5f));
    }
}
