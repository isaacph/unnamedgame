package render;

import game.Camera;
import model.GameObjectID;
import model.World;
import org.joml.*;
import model.GameData;
import model.GameObject;
import model.GameObjectType;

public class TeamTextureComponent extends RenderComponent {

    private World world;
    private Texture texture, textureOutline;
    private Vector2f renderOffset;
    private Vector2f renderScale;
    private GameObjectID gameObjectID;
    private GameData gameData;
    private Vector2f position;
    private boolean fixedPosition = true;
    private Vector2f centerOffset;
    private boolean forceVisible = false;
    private boolean forcedVisiblity = false;
    private Vector2f circleOffset;
    private float circleSize;

    public TeamTextureComponent(GameObjectID gameObjectID, World world, GameData gameData, WorldRenderer.GameObjectTextures textureLibrary,
                                String texturePath, Vector2f texOffset, Vector2f texScale, Vector2f centerOffset, Vector2f circleOffset, float circleSize) {
        this.world = world;
        this.gameData = gameData;
        this.gameObjectID = gameObjectID;
        GameObject obj = world.gameObjects.get(gameObjectID);
        this.texture = textureLibrary.getTexture(texturePath);
        this.renderOffset = texOffset;
        this.renderScale = texScale;
        this.position = new Vector2f(obj.x, obj.y);
        this.centerOffset = centerOffset;
        this.circleOffset = circleOffset;
        this.circleSize = circleSize;
    }

    private boolean visible(GameObject obj) {
        return forceVisible ? forcedVisiblity : obj.alive;
    }

    @Override
    public void drawGround(WorldRenderer renderer, Matrix4f orthoProj) {
        GameObject gameObject = world.gameObjects.get(gameObjectID);
        if(gameObject != null && visible(gameObject)) {
            GameObjectType type = gameData.getType(gameObject.type);
            if(fixedPosition) {
                this.position = new Vector2f(gameObject.x, gameObject.y);
            }
            Vector2f pos = Camera.worldToViewSpace(new Vector2f(position).add(0.5f, 0.5f).add(circleOffset));
            Vector3f color = world.teams.getTeamColor(gameObject.team);
            renderer.ellipseRenderer.draw(new Matrix4f(orthoProj).translate(pos.x, pos.y, 0)
                    .scale(1.0f * circleSize, TileGridRenderer.TILE_RATIO * circleSize, 0),
                    new Vector4f(color.x, color.y, color.z, 0.6f), 1.0f, TileGridRenderer.TILE_RATIO);
        }
    }

    @Override
    public void draw(WorldRenderer renderer, Matrix4f orthoProj) {
        GameObject gameObject = world.gameObjects.get(gameObjectID);
        if(gameObject != null && visible(gameObject)) {
            if(fixedPosition) {
                this.position = new Vector2f(gameObject.x, gameObject.y);
            }
            Vector2f pos = Camera.worldToViewSpace(position);
            texture.bind();
            renderer.textureRenderer.draw(new Matrix4f(orthoProj)
                    .translate(renderOffset.x + pos.x, renderOffset.y + pos.y, 0)
                    .scale(renderScale.x, renderScale.y, 0),
                    new Vector4f(1));
        }
    }

    public Vector2f getRenderPosition() {
        GameObject gameObject = world.gameObjects.get(gameObjectID);
        if(gameObject == null) return new Vector2f(0);
        if(fixedPosition) {
            this.position = new Vector2f(gameObject.x, gameObject.y);
        }
        return Camera.worldToViewSpace(position);
    }

    public Vector2f getWorldCenter() {
        return new Vector2f(position).add(centerOffset);
    }

    public void move(Vector2f position) {
        this.fixedPosition = false;
        this.position = new Vector2f(position);
    }

    public void resetPosition() {
        this.fixedPosition = true;
    }

    @Override
    public void forceVisible(boolean visible) {
        this.forceVisible = true;
        this.forcedVisiblity = visible;
    }

    @Override
    public void resetVisible() {
        this.forceVisible = false;
    }
}
