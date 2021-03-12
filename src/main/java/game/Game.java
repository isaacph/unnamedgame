package game;

import org.joml.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import render.*;
import staticData.GameData;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {

    // The window handle
    private long window;
    private BoxRenderer boxRenderer;
    private TextureRenderer textureRenderer;
    private Font font;

    // this state info will likely be moved
    private int screenWidth = 800, screenHeight = 600;
    private final Vector2f mousePosition = new Vector2f();
    private final Vector2i mouseWorldPosition = new Vector2i();

    private Camera camera;
    private Chatbox chatbox;
    private GameObjectFactory gameObjectFactory;

    private World world;
    private WorldRenderer worldRenderer;
    private ActionManager actionManager;
    private ClickBoxManager clickBoxManager;

    private GameData gameData;
    private GameTime gameTime;

    private Mode mode = Mode.PLAY;
    private GameObject selectedObject = null;
    private UICommand currentCommand = UICommand.NONE;

    enum Mode {
        PLAY, EDIT
    }
    enum UICommand {
        NONE, MOVE
    }

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
        });
        glfwSetWindowSizeCallback(window, (win, w, h) -> {
            windowResize(w, h);
        });
        glfwSetCharCallback(window, (win, codepoint) -> {
            if(chatbox.focus) {
                chatbox.typing.append((char) codepoint);
            }
        });
        glfwSetMouseButtonCallback(window, ((window1, button, action, mods) -> {
            mouseButton(button, action, mods);
        }));
        glfwSetKeyCallback(window, ((window1, key, scancode, action, mods) -> {
            keyboardButton(key, scancode, action, mods);
        }));

        this.boxRenderer = new BoxRenderer();
        this.textureRenderer = new TextureRenderer();
        this.font = new Font("font.ttf", 48, 512, 512);

        this.gameData = new GameData();
        this.world = new World();
        this.worldRenderer = new WorldRenderer(camera, gameData, world, gameTime);
        this.actionManager = new ActionManager();

        this.gameTime = new GameTime(window);
        this.chatbox = new Chatbox(font, boxRenderer, gameTime);
        this.camera = new Camera(gameTime, window);
        this.clickBoxManager = new ClickBoxManager(world, gameData, camera, worldRenderer);

        this.gameObjectFactory = new GameObjectFactory();

        windowResize(screenWidth, screenHeight);
    }

    private void windowResize(int width, int height) {
        glViewport(0, 0, width, height);
        screenWidth = width;
        screenHeight = height;

        camera.windowResize(width, height);
    }

    private void pollMousePosition() {
        double[] mx = new double[1], my = new double[1];
        glfwGetCursorPos(window, mx, my);
        mousePosition.x = (float) mx[0];
        mousePosition.y = (float) my[0];
        Vector2f pos = camera.screenToWorldSpace(mousePosition);
        mouseWorldPosition.set(Util.floor(pos.x), Util.floor(pos.y));
    }

    private void mouseButton(int button, int action, int mods) {
        if(mode == Mode.PLAY) {
            if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                if(currentCommand == UICommand.NONE) {
                    selectedObject = clickBoxManager.getGameObjectAtScreenPosition(mousePosition);
                } else if(currentCommand == UICommand.MOVE) {
                    if(!mouseWorldPosition.equals(selectedObject.x, selectedObject.y)) {
                        actionManager.startAction(
                            new MoveAction(gameTime, actionManager, world, worldRenderer, gameData, selectedObject.uniqueID, mouseWorldPosition));
                        selectedObject.x = mouseWorldPosition.x;
                        selectedObject.y = mouseWorldPosition.y;
                        currentCommand = UICommand.NONE;
                    }
                }
            }
        }
    }

    private void keyboardButton(int key, int scancode, int action, int mods) {
        if(chatbox.focus) {
            if(key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                if (!chatbox.send()) {
                    chatbox.disable();
                }
            }
            if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                chatbox.disable();
            } else if(key == GLFW_KEY_BACKSPACE && action > 0) {
                if(chatbox.typing.length() > 0) {
                    chatbox.typing.deleteCharAt(chatbox.typing.length() - 1);
                }
            }
        } else {
            if(key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                chatbox.enable();
            } else if(mode == Mode.PLAY) {
                if (currentCommand != UICommand.NONE) {
                    if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                        currentCommand = UICommand.NONE;
                    }
                }
                if (currentCommand == UICommand.NONE) {
                    if (key == GLFW_KEY_SPACE && action == GLFW_PRESS) {
                        currentCommand = UICommand.MOVE;
                    }
                }
            }
        }
    }

    private void loop() {

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {

            gameTime.update();

            // Poll for window events. Invokes window callbacks
            pollMousePosition();
            glfwPollEvents();
            camera.move();

            if(mode == Mode.EDIT) {
                if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
                    if (world.grid.getTile(mouseWorldPosition.x, mouseWorldPosition.y) != 1) {
                        Grid updateGrid = world.grid.setTile((byte) 1, mouseWorldPosition.x, mouseWorldPosition.y);
                        worldRenderer.tileGridRenderer.build(updateGrid);
                    }
                } else if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
                    if (world.grid.getTile(mouseWorldPosition.x, mouseWorldPosition.y) != 0) {
                        Grid updateGrid = world.grid.setTile((byte) 0, mouseWorldPosition.x, mouseWorldPosition.y);
                        worldRenderer.tileGridRenderer.build(updateGrid);
                    }
                } else if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
                    GameObject obj = gameObjectFactory.createGameObject(gameData.getPlaceholder());
                    obj.x = mouseWorldPosition.x;
                    obj.y = mouseWorldPosition.y;
                    if (world.add(obj)) {
                        worldRenderer.resetGameObjectRenderCache();
                        clickBoxManager.resetGameObjectCache();
//                    actionManager.startAction(new MoveAction(gameTime,
//                        actionManager,
//                        world,
//                        worldRenderer,
//                        gameData,
//                        obj.uniqueID,
//                        new Vector2i(0, 0)));
//                    obj.x = 0;
//                    obj.y = 0;
                    }
                }
            } else if(mode == Mode.PLAY) {
            }

            chatbox.update();
            for(String cmd : chatbox.commands) {
                if(cmd.startsWith("/")) {
                    String[] args = cmd.substring(1).split("\\s");
                    if(args[0].equals("test")) {
                        chatbox.println("Testing!");
                    } else if(args[0].equals("edit")) {
                        mode = Mode.EDIT;
                        chatbox.println("Editing enabled");
                    } else if(args[0].equals("play")) {
                        mode = Mode.PLAY;
                        chatbox.println("Gameplay enabled");
                    } else {
                        chatbox.println("Unknown command!");
                    }
                } else {
                    chatbox.println(cmd);
                }
            }
            chatbox.commands.clear();

            actionManager.update();
            worldRenderer.update();

            // all updates go here

            glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer

            // everything drawing goes here
            camera.updateView();

            if(selectedObject == null) {
                worldRenderer.setMouseWorldPosition(new Vector2i(mouseWorldPosition));
            } else {
                worldRenderer.setMouseWorldPosition(new Vector2i(selectedObject.x, selectedObject.y));
            }
            worldRenderer.draw(camera);

            chatbox.draw(camera.getProjection());

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