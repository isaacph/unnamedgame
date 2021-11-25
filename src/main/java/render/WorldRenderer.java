package render;

import game.*;
import model.*;
import model.grid.ByteGrid;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;

import java.util.*;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public class WorldRenderer {

    public final BoxRenderer boxRenderer;
    public final TextureRenderer textureRenderer;
    public final SpriteRenderer spriteRenderer;
    public final TileGridRenderer tileGridRenderer;
    public final EllipseRenderer ellipseRenderer;

    private final Collection<Vector2i> mouseWorldPosition = new ArrayList<>();
    private final World world;
    private final VisualData visualData;
    private final GameData gameData;

    private final ArrayList<RenderComponent> gameObjectRenderer = new ArrayList<>();
    private final Map<GameObjectID, RenderComponent> gameObjectIDMap = new HashMap<>();
    private final GameObjectTextures textureLibrary = new GameObjectTextures();

    private Camera camera;
    private GameTime time;

    private final Font font;

    public WorldRenderer(Camera camera, GameData gameData, VisualData visualData, World world, GameTime gameTime) {
        this.camera = camera;
        this.world = world;
        this.visualData = visualData;
        this.gameData = gameData;
        this.boxRenderer = new BoxRenderer();
        this.textureRenderer = new TextureRenderer();
        this.spriteRenderer = new SpriteRenderer();
        this.tileGridRenderer = new TileGridRenderer();
        this.ellipseRenderer = new EllipseRenderer();
        this.time = gameTime;
        this.font = new Font("font.ttf", 32, 512, 512);
    }

    public void update() {

    }

    public void resetGameObjectRenderCache() {
        gameObjectRenderer.clear();
        gameObjectIDMap.clear();
        for(GameObject obj : world.gameObjects.values()) {
            RenderComponent rc = makeRenderComponent(obj);
            gameObjectRenderer.add(rc);
            gameObjectIDMap.put(obj.uniqueID, rc);
        }
    }

    public void rebuildTerrain() {
        tileGridRenderer.clear();
        for(ByteGrid grid : world.grid.map.values()) {
            tileGridRenderer.build(grid);
        }
    }

    public void setMouseWorldPosition(Collection<Vector2i> v) {
        this.mouseWorldPosition.clear();
        this.mouseWorldPosition.addAll(v);
    }

    public void draw(Camera camera) {
        tileGridRenderer.draw(new Matrix4f(camera.getProjView()), new Vector4f(1), camera.getScaleFactor());

        for(Vector2i pos : mouseWorldPosition) {
            Vector2f tilePos = Camera.worldToViewSpace(new Vector2f(pos.x + 0.5f, pos.y + 0.5f));
            boxRenderer.draw(new Matrix4f(camera.getProjView()).translate(tilePos.x, tilePos.y, 0).scale(1, TileGridRenderer.TILE_RATIO, 1)
                    .rotate(45 * (float) Math.PI / 180.0f, 0, 0, 1), new Vector4f(1.0f, 1.0f, 1.0f, 0.4f));
        }

        tileGridRenderer.drawSelect(camera.getProjView(), camera.getScaleFactor());

        Collections.sort(gameObjectRenderer);
        for(RenderComponent renderer : gameObjectRenderer) {
            renderer.drawGround(this, camera.getProjView());
        }

        for(RenderComponent renderer : gameObjectRenderer) {
            renderer.draw(this, camera.getProjView());
        }
    }

    public RenderComponent getGameObjectRenderer(GameObjectID id) {
        return gameObjectIDMap.get(id);
    }

    private RenderComponent makeRenderComponent(GameObject gameObject) {
        VisualDataType type = visualData.getType(gameObject.type);
        if(type == null) throw new RuntimeException("No visual data found for " + gameObject.type.getName());
        return type.makeRenderComponent(gameObject.uniqueID, world, gameData, textureLibrary);
    }

    public static class GameObjectTextures {
        private final Map<String, Texture> texturesLoaded = new HashMap<>();

        public Texture getTexture(String path) {
            Texture texture = texturesLoaded.get(path);
            if(texture == null) {
                texture = Texture.makeTexture(path, new Texture.Settings(GL_CLAMP_TO_EDGE, GL_NEAREST));
                texturesLoaded.put(path, texture);
            }
            return texture;
        }
    }
}
