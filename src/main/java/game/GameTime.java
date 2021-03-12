package game;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class GameTime {

    /**
     * The number of seconds since the last frame
     */
    private double delta;

    /* variables for calculating delta */
    private double currentTime;
    private double lastTime;

    private final long window;

    public GameTime(long glfwWindow) {
        window = glfwWindow;
        currentTime = glfwGetTime();
        lastTime = currentTime;
    }

    public void update() {
        currentTime = glfwGetTime();
        delta = currentTime - lastTime; // almost always 0.01666666666666666
        lastTime = currentTime;
    }

    public double getDelta() {
        return delta;
    }
}
