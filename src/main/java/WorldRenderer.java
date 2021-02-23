import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public class WorldRenderer {

    public final BoxRenderer boxRenderer;
    public final TextureRenderer textureRenderer;
    public final SpriteRenderer spriteRenderer;
    public final TileGridRenderer tileGridRenderer;

    private final Vector2i mouseWorldPosition = new Vector2i();
    private final World world;
    private final GameData gameData;

    private final ArrayList<RenderComponent> gameObjectRenderer = new ArrayList<>();
    private final Map<Long, RenderComponent> gameObjectRendererIndex = new HashMap<>();
    private final GameObjectTextures textureLibrary = new GameObjectTextures();

    public WorldRenderer(GameData gameData, World world) {
        this.world = world;
        this.gameData = gameData;
        this.boxRenderer = new BoxRenderer();
        this.textureRenderer = new TextureRenderer();
        this.spriteRenderer = new SpriteRenderer();
        this.tileGridRenderer = new TileGridRenderer();
    }

    public void update(double delta) {

    }

    public void resetGameObjectRenderCache() {
        gameObjectRenderer.clear();
        gameObjectRendererIndex.clear();
        for(GameObject obj : world.gameObjects) {
            RenderComponent rc = getRenderComponent(obj);
            gameObjectRenderer.add(rc);
            gameObjectRendererIndex.put(obj.uniqueID, rc);
        }
        Collections.sort(gameObjectRenderer);
    }

    public void setMouseWorldPosition(Vector2i v) {
        this.mouseWorldPosition.set(v);
    }

    public void draw(Matrix4f projView, Camera camera) {
        tileGridRenderer.draw(new Matrix4f(projView), new Vector4f(1), camera.getScaleFactor());

        Vector2f tilePos = Camera.worldToViewSpace(new Vector2f(mouseWorldPosition.x + 0.5f, mouseWorldPosition.y + 0.5f));
        boxRenderer.draw(new Matrix4f(projView).translate(tilePos.x, tilePos.y, 0).scale(1, TileGridRenderer.TILE_RATIO, 1)
            .rotate(45 * (float) Math.PI / 180.0f, 0, 0, 1), new Vector4f(0.4f));

        for(RenderComponent renderer : gameObjectRenderer) {
            renderer.draw(this, projView);
        }
    }

    private RenderComponent getRenderComponent(GameObject gameObject) {
        return new TextureComponent(gameData, gameObject, textureLibrary);
    }

    public static class GameObjectTextures {
        private final Map<String, Texture> texturesLoaded = new HashMap<>();

        public Texture getTexture(String path) {
            Texture texture = texturesLoaded.get(path);
            if(texture == null) {
                texture = new Texture(path, new Texture.Settings(GL_CLAMP_TO_EDGE, GL_NEAREST));
                texturesLoaded.put(path, texture);
            }
            return texture;
        }
    }
}
