package render;

import game.Camera;
import game.GameObjectID;
import game.World;
import org.joml.*;
import staticData.GameData;
import game.GameObject;
import staticData.GameObjectType;

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

    public TeamTextureComponent(GameObjectID gameObjectID, World world, GameData gameData, WorldRenderer.GameObjectTextures textureLibrary) {
        this.world = world;
        this.gameData = gameData;
        this.gameObjectID = gameObjectID;
        GameObject obj = world.gameObjects.get(gameObjectID);
        GameObjectType type = gameData.getType(obj.type);
        this.texture = textureLibrary.getTexture(type.getTexturePath());
        this.renderOffset = type.getTextureOffset();
        this.renderScale = type.getTextureScale();
        this.position = new Vector2f(obj.x, obj.y);
        this.centerOffset = type.getCenterOffset();
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
            Vector2f pos = Camera.worldToViewSpace(new Vector2f(position).add(0.5f, 0.5f).add(type.getCircleOffset()));
            Vector3f color = world.teams.getTeamColor(gameObject.team);
            renderer.ellipseRenderer.draw(new Matrix4f(orthoProj).translate(pos.x, pos.y, 0)
                    .scale(1.0f * type.getCircleSize(), TileGridRenderer.TILE_RATIO * type.getCircleSize(), 0),
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
