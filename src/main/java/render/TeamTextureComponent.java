package render;

import game.Camera;
import model.*;
import org.joml.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TeamTextureComponent extends RenderComponent {

    private World world;
    private List<RenderInfo> renderInfo;
    private GameObjectID gameObjectID;
    private GameData gameData;
    private Vector2f position;
    private boolean fixedPosition = true;
    private Vector2f centerOffset;
    private boolean forceVisible = false;
    private boolean forcedVisiblity = false;
    private Vector2f circleOffset;
    private float circleSize;

    public TeamTextureComponent(GameObjectID gameObjectID, World world, GameData gameData,
                                Collection<RenderInfo> renderInfo, Vector2f centerOffset, Vector2f circleOffset, float circleSize) {
        this.world = world;
        this.gameData = gameData;
        this.gameObjectID = gameObjectID;
        GameObject obj = world.gameObjects.get(gameObjectID);
        this.renderInfo = new ArrayList<>(renderInfo);
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
        if(gameObject != null && visible(gameObject) && !gameObject.team.equals(TeamID.NEUTRAL)) {
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
    public Collection<OrderedDrawCall> draw(WorldRenderer renderer, Matrix4f orthoProj) {
        List<OrderedDrawCall> drawCalls = new ArrayList<>();
        for(RenderInfo info : renderInfo) {
            drawCalls.add(new OrderedDrawCall() {
                @Override
                public void draw() {
                    Texture texture = info.texture;
                    Vector2f renderOffset = info.offset;
                    Vector2f renderScale = new Vector2f(info.size);
                    GameObject gameObject = world.gameObjects.get(gameObjectID);
                    if(gameObject != null && visible(gameObject)) {
                        if(fixedPosition) {
                            position = new Vector2f(gameObject.x, gameObject.y);
                        }
                        Vector2f pos = Camera.worldToViewSpace(position);
                        texture.bind();
                        renderer.textureRenderer.draw(new Matrix4f(orthoProj)
                                        .translate(renderOffset.x + pos.x, renderOffset.y + pos.y, 0)
                                        .scale(renderScale.x, renderScale.y, 0),
                                new Vector4f(1));
                    }
                }

                @Override
                public float getScreenPositionY() {
                    return getScreenRenderPosition(info.depthOffset).y();
                }
            });
        }
        return drawCalls;
    }

    public Vector2f getScreenRenderPosition(float depthOffset) {
        GameObject gameObject = world.gameObjects.get(gameObjectID);
        if(gameObject == null) return new Vector2f(0);
        if(fixedPosition) {
            this.position = new Vector2f(gameObject.x, gameObject.y);
        }
        Vector2f output = Camera.worldToViewSpace(position);
        output.y += depthOffset;
        return output;
    }

    public Vector2f getScreenRenderPosition() {
        return getScreenRenderPosition(0);
    }

    public Vector2f getCenterOffset() {
        return new Vector2f(centerOffset);
    }

    public Vector2f getWorldCenter() {
        return new Vector2f(position).add(centerOffset);
    }

    public void move(Vector2f position) {
        this.fixedPosition = false;
        this.position = new Vector2f(position).sub(centerOffset);
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

    public static class RenderInfo {
        public Texture texture;
        public float size;
        public Vector2f offset;
        public float depthOffset;

        public RenderInfo(Texture texture, float size, Vector2fc offset, float depthOffset) {
            this.texture = texture;
            this.size = size;
            this.offset = new Vector2f(offset);
            this.depthOffset = depthOffset;
        }
    }
}
