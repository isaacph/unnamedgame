import org.joml.*;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {

    // The window handle
    private long window;
    private BoxRenderer boxRenderer;
    private TextureRenderer textureRenderer;
    private TileGridRenderer tileGridRenderer;
    private Texture rocket;
    private Font font;

    private int screenWidth = 800, screenHeight = 600;

    private Matrix4f proj = new Matrix4f();
    private Matrix4f projView = new Matrix4f();
    public Vector2f mousePosition = new Vector2f();

    private Grid.Group grid;
    public Camera camera;

    /**
     * The number of seconds since the last frame
     */
    private double delta;

    public void run() {
        init();
        loop();
        cleanUp();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(screenWidth, screenHeight, "Unnamed Game", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the background color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Enable blending (properly handling alpha values)
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(window, true);
            }
            if(key == GLFW_KEY_BACKSPACE && action >= GLFW_PRESS) {
                System.out.println("BACKSPACE");
            }
            if(key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                System.out.println("ENTER");
            }
        });
        glfwSetWindowSizeCallback(window, (win, w, h) -> {
            windowResize(w, h);
        });
        glfwSetCharCallback(window, (win, codepoint) -> {
//            System.out.print((char) codepoint);
        });

        this.boxRenderer = new BoxRenderer();
        this.textureRenderer = new TextureRenderer();
        this.rocket = new Texture("rocket1.png");
        this.font = new Font("font.ttf", 48, 512, 512);
        this.tileGridRenderer = new TileGridRenderer();
        this.grid = new Grid.Group();
//        grid.setTile((byte) 1, 0, 0);
//        grid.setTile((byte) 1, 1, 1);
//        grid.setTile((byte) 1, 1, 0);
//        grid.setTile((byte) 1, 2, 0);
//        grid.setTile((byte) 1, 3, 0);
//        grid.setTile((byte) 1, 3, 1);
//        this.tileGridRenderer.build(grid.map.get(new Vector2i(0, 0)));
        camera = new Camera();

        windowResize(screenWidth, screenHeight);
    }

    private void windowResize(int width, int height) {
        glViewport(0, 0, width, height);
        screenWidth = width;
        screenHeight = height;
        proj = new Matrix4f().ortho(0, width, height, 0, 0.0f, 1.0f);

        camera.windowResize(width, height);

        projView = new Matrix4f(proj).mul(camera.getView());
    }

    private void pollMousePosition() {
        double[] mx = new double[1], my = new double[1];
        glfwGetCursorPos(window, mx, my);
        mousePosition.x = (float) mx[0];
        mousePosition.y = (float) my[0];
    }

    private void loop() {
        // variables for calculating delta
        // also determines the amount of delta in the first frame
        double currentTime = glfwGetTime();
        double lastTime = currentTime;

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            currentTime = glfwGetTime();
            delta = currentTime - lastTime; // almost always 0.01666666666666666
            lastTime = currentTime;

            // Poll for window events. Invokes window callbacks
            pollMousePosition();
            glfwPollEvents();
            camera.move(window, delta);

            if(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
                Vector2f worldSpace = camera.screenToWorldSpace(mousePosition);
                if(grid.getTile(worldSpace.x, worldSpace.y) != 1) {
                    Grid updateGrid = grid.setTile((byte) 1, worldSpace.x, worldSpace.y);
                    tileGridRenderer.build(updateGrid);
                }
            }
            else if(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
                Vector2f worldSpace = camera.screenToWorldSpace(mousePosition);
                if(grid.getTile(worldSpace.x, worldSpace.y) != 0 || true) {
                    Grid updateGrid = grid.setTile((byte) 0, worldSpace.x, worldSpace.y);
                    tileGridRenderer.build(updateGrid);
                }
            }

            tileGridRenderer.update(delta, window, this);

            // all updates go here

            glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer

            // everything drawing goes here
            camera.updateView();
            projView = new Matrix4f(proj).mul(camera.getView());

            tileGridRenderer.draw(new Matrix4f(projView), new Vector4f(1), camera.getScaleFactor());
//            rocket.bind();

            boxRenderer.draw(new Matrix4f(projView).translate(650, 50, 0).scale(400, 200, 0), new Vector4f(0, 0, 0, 0.5f));
            font.draw("asjdfoiajsdiofajsoi", 500, 100, new Matrix4f(proj), new Vector4f(1));

            glfwSwapBuffers(window); // swap the color buffers, rendering what was drawn to the screen
        }

        Shaders.checkGLError("End main loop");
    }

    public void cleanUp() {
        boxRenderer.cleanUp();
        textureRenderer.cleanUp();
    }

    public static void main(String[] args) {
        new Game().run();
    }

}