package render;

import game.Camera;
import org.joml.Vector2i;
import staticData.GameData;
import game.GameObject;
import staticData.GameObjectType;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class TextureComponent extends RenderComponent {

    private Texture texture;
    private Vector2f renderOffset;
    private Vector2f renderScale;
    private GameObject gameObject;
    private Vector2f position;
    private boolean fixedPosition = true;

    public TextureComponent(GameData gameData, GameObject gameObject, WorldRenderer.GameObjectTextures textureLibrary) {
        GameObjectType type = gameData.getType(gameObject.type);
        this.texture = textureLibrary.getTexture(type.texture);
        this.renderOffset = type.textureOffset;
        this.renderScale = type.textureScale;
        this.gameObject = gameObject;
        this.position = new Vector2f(gameObject.x, gameObject.y);
    }

    @Override
    public void draw(WorldRenderer renderer, Matrix4f orthoProj) {
        texture.bind();
        if(fixedPosition) {
            this.position = new Vector2f(gameObject.x, gameObject.y);
        }
        Vector2f pos = Camera.worldToViewSpace(position);
        renderer.textureRenderer.draw(new Matrix4f(orthoProj)
            .translate(renderOffset.x + pos.x, renderOffset.y + pos.y, 0)
            .scale(renderScale.x, renderScale.y, 0),
            new Vector4f(1));
    }

    public Vector2f getRenderPosition() {
        if(fixedPosition) {
            this.position = new Vector2f(gameObject.x, gameObject.y);
        }
        Vector2f pos = Camera.worldToViewSpace(position);
        return Camera.worldToViewSpace(pos);
    }

    public void move(Vector2f position) {
        this.fixedPosition = false;
        this.position = new Vector2f(position);
    }

    public void resetPosition() {
        this.fixedPosition = true;
    }
}
