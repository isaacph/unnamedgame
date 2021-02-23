import org.joml.*;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.lang.Math;
import java.nio.*;
import java.util.ArrayList;
import java.util.Collections;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {

    // The window handle
    private long window;
    private BoxRenderer boxRenderer;
    public TextureRenderer textureRenderer;
    private TileGridRenderer tileGridRenderer;
    private Texture rocket;
    private Font font;

    private int screenWidth = 800, screenHeight = 600;

    private Matrix4f proj = new Matrix4f();
    private Matrix4f projView = new Matrix4f();
    public Vector2f mousePosition = new Vector2f();
    public Vector2i mouseWorldPosition = new Vector2i();

    private Grid.Group grid;
    public Camera camera;
    public Chatbox chatbox;
    public GameObjectFactory gameObjectFactory;

    public World world = new World();

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
            if(key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                if(chatbox.focus) {
                    if (!chatbox.send()) {
                        chatbox.disable();
                    }
                } else {
                    chatbox.enable();
                }
            }
            if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                if(chatbox.focus) {
                    chatbox.disable();
                }
            } else if(key == GLFW_KEY_BACKSPACE && action > 0) {
                if(chatbox.typing.length() > 0) {
                    chatbox.typing.deleteCharAt(chatbox.typing.length() - 1);
                }
            }
        });
        glfwSetWindowSizeCallback(window, (win, w, h) -> {
            windowResize(w, h);
        });
        glfwSetCharCallback(window, (win, codepoint) -> {
            if(chatbox.focus) {
                chatbox.typing.append((char) codepoint);
            }
        });

        this.boxRenderer = new BoxRenderer();
        this.textureRenderer = new TextureRenderer();
        this.rocket = new Texture("rocket1.png");
        this.font = new Font("font.ttf", 48, 512, 512);
        this.tileGridRenderer = new TileGridRenderer();
        this.grid = new Grid.Group();
        this.chatbox = new Chatbox(font, boxRenderer);
        camera = new Camera();

        this.gameObjectFactory = new GameObjectFactory();

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
        Vector2f pos = camera.screenToWorldSpace(mousePosition);
        mouseWorldPosition = new Vector2i(Util.floor(pos.x), Util.floor(pos.y));
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
                if(grid.getTile(mouseWorldPosition.x, mouseWorldPosition.y) != 1) {
                    Grid updateGrid = grid.setTile((byte) 1, mouseWorldPosition.x, mouseWorldPosition.y);
                    tileGridRenderer.build(updateGrid);
                }
            }
            else if(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
                if(grid.getTile(mouseWorldPosition.x, mouseWorldPosition.y) != 0) {
                    Grid updateGrid = grid.setTile((byte) 0, mouseWorldPosition.x, mouseWorldPosition.y);
                    tileGridRenderer.build(updateGrid);
                }
            }
            else if(glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
                GameObject obj = gameObjectFactory.createGameObject(GameObjectFactory.kid);
                obj.x = mouseWorldPosition.x;
                obj.y = mouseWorldPosition.y;
                world.add(obj);
            }

            tileGridRenderer.update(delta, window, this);
            chatbox.update(delta);
            for(String cmd : chatbox.commands) {
                if(cmd.startsWith("/")) {
                    String[] args = cmd.substring(1).split("\\s");
                    if(args[0].equals("test")) {
                        chatbox.println("Testing!");
                    } else {
                        chatbox.println("Unknown command!");
                    }
                } else {
                    chatbox.println(cmd);
                }
            }
            chatbox.commands.clear();

            world.renderUpdate(this, delta);

            // all updates go here

            glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer

            // everything drawing goes here
            camera.updateView();
            projView = new Matrix4f(proj).mul(camera.getView());

            tileGridRenderer.draw(new Matrix4f(projView), new Vector4f(1), camera.getScaleFactor());

            Vector2f tilePos = Camera.worldToViewSpace(new Vector2f(mouseWorldPosition.x + 0.5f, mouseWorldPosition.y + 0.5f));
            boxRenderer.draw(new Matrix4f(projView).translate(tilePos.x, tilePos.y, 0).scale(1, TileGridRenderer.TILE_RATIO, 1)
                .rotate(45 * (float) Math.PI / 180.0f, 0, 0, 1), new Vector4f(0.4f));

            world.draw(this, projView);

            chatbox.draw(new Matrix4f(proj));

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