package game;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import render.TileGridRenderer;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {

    private long window;
    private GameTime time;

    private float scale = 12.0f;
    private float scaleFactor;
    private Vector2f center = new Vector2f();

    private Matrix4f projection = new Matrix4f();
    private Matrix4f projView = new Matrix4f();
    private Matrix4f view = new Matrix4f();
    private Matrix4f viewInv = new Matrix4f();
    private int windowWidth, windowHeight;
    public static final Matrix4f viewToWorld = new Matrix4f()
        .rotate(-45.0f * (float) Math.PI / 180.0f, 0, 0, 1)
        .scale(TileGridRenderer.TILE_WIDTH, 1.0f / TileGridRenderer.TILE_RATIO * TileGridRenderer.TILE_WIDTH, 1);
    public static final Matrix4f worldToView = new Matrix4f(viewToWorld).invert();

    public Camera(GameTime gameTime, long glfwWindow) {
        window = glfwWindow;
        time = gameTime;
    }

    public void move() {
        Vector2f dpos = new Vector2f();
        if(glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
            dpos.y--;
        }
        if(glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
            dpos.y++;
        }
        if(glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) {
            dpos.x--;
        }
        if(glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) {
            dpos.x++;
        }
        if(dpos.lengthSquared() > 0) {
            dpos.normalize((float) time.getDelta() * 5.0f);
            center.add(dpos);
        }
    }

    public void updateView() {
        scaleFactor = Float.min(windowWidth / scale, windowHeight / scale);
        view.identity();
        view.translate(windowWidth / 2.0f, windowHeight / 2.0f, 0);
        view.scale(scaleFactor);
        view.translate(-center.x, -center.y, 0);
        viewInv.set(view).invert();
        this.projView.set(projection).mul(view);
    }

    public void windowResize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
        this.projection.identity().ortho(0, width, height, 0, 0.0f, 1.0f);
        this.updateView();
        this.projView.set(this.projection).mul(this.view);
    }

    public Matrix4f getView() {
        return view;
    }
    public Matrix4f getProjection() { return projection; }
    public Matrix4f getProjView() { return projView; }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public Vector2f getCenter() {
        return center;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setCenter(Vector2f center) {
        this.center = new Vector2f(center);
    }

    public Vector2f screenToViewSpace(Vector2f screenSpace) {
        Vector4f v = new Vector4f(screenSpace.x, screenSpace.y, 0, 1).mul(viewInv);
        return new Vector2f(v.x * v.w, v.y * v.w);
    }

    public Vector2f viewToScreenSpace(Vector2f viewSpace) {
        Vector4f v = new Vector4f(viewSpace.x, viewSpace.y, 0, 1).mul(view);
        return new Vector2f(v.x * v.w, v.y * v.w);
    }

    public Vector2f worldToScreenSpace(Vector2f worldSpace) {
        return viewToScreenSpace(worldToViewSpace(worldSpace));
    }

    public static Vector2f viewToWorldSpace(Vector2f viewSpace) {
        Vector4f v = new Vector4f(viewSpace.x, viewSpace.y, 0, 1).mul(viewToWorld);
        return new Vector2f(v.x * v.w, v.y * v.w);
    }

    public static Vector2f worldToViewSpace(Vector2f worldSpace) {
        Vector4f v = new Vector4f(worldSpace.x, worldSpace.y, 0, 1).mul(worldToView);
        return new Vector2f(v.x * v.w, v.y * v.w);
    }

    public Vector2f screenToWorldSpace(Vector2f screenSpace) {
        return viewToWorldSpace(screenToViewSpace(screenSpace));
    }
}
