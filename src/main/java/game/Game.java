package game;

import org.joml.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import render.*;
import server.EchoPayload;
import server.Server;
import server.ServerPayload;
import staticData.GameData;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

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
    private final Vector2f mouseViewPosition = new Vector2f();

    private Camera camera;
    private Chatbox chatbox;
    private GameObjectFactory gameObjectFactory;

    private World world;
    private WorldRenderer worldRenderer;
    private AnimationManager animationManager;
    private ClickBoxManager clickBoxManager;

    private GameData gameData;
    private GameTime gameTime;

    private GameResources consRes;

    private Mode mode = Mode.PLAY;
    private int selectedID = -1;
    private UICommand currentCommand = UICommand.NONE;

    private SocketChannel socketChannel;

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
        this.animationManager = new AnimationManager();

        this.gameTime = new GameTime(window);
        this.chatbox = new Chatbox(font, boxRenderer, gameTime);
        this.camera = new Camera(gameTime, window);
        this.clickBoxManager = new ClickBoxManager(world, gameData, camera, worldRenderer);

        this.gameObjectFactory = new GameObjectFactory();
        this.consRes = new GameResources(camera, chatbox, gameObjectFactory, world, worldRenderer, animationManager, clickBoxManager, gameData, gameTime);

        windowResize(screenWidth, screenHeight);

        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress("localhost", Server.PORT));
            EchoPayload p = new EchoPayload("echo hello world");
            try(ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(p);
                System.out.println(new String(bos.toByteArray(), StandardCharsets.UTF_8));
                socketChannel.write(ByteBuffer.wrap(bos.toByteArray()));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
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
        Vector2f vpos = camera.screenToViewSpace(mousePosition);
        Vector2f pos = Camera.viewToWorldSpace(vpos);
        mouseWorldPosition.set(Util.floor(pos.x), Util.floor(pos.y));
        mouseViewPosition.set(vpos);
    }

    private void mouseButton(int button, int action, int mods) {
        if(mode == Mode.PLAY) {
            if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                if(currentCommand == UICommand.NONE) {
                    GameObject selectedObject = clickBoxManager.getGameObjectAtViewPosition(mouseViewPosition);
                    if(selectedObject != null) {
                        selectedID = selectedObject.uniqueID;
                    } else {
                        selectedID = -1;
                    }
                } else {
                    if(!animationManager.isObjectOccupied(selectedID)) {
                        GameObject selectedObject = world.gameObjects.get(selectedID);
                        if (currentCommand == UICommand.MOVE) {
                            if (!mouseWorldPosition.equals(selectedObject.x, selectedObject.y)
                                && !world.occupied(mouseWorldPosition.x, mouseWorldPosition.y)) {
                                new MoveAction(selectedID, mouseWorldPosition.x, mouseWorldPosition.y).animate(consRes);
                            }
                            currentCommand = UICommand.NONE;
                        }
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
            if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                currentCommand = UICommand.NONE;
            }
            if(key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                chatbox.enable();
            } else if(mode == Mode.PLAY) {
                if (currentCommand == UICommand.NONE && !animationManager.isObjectOccupied(selectedID)) { // no command is currently selected
                    if(selectedID != -1 && key == GLFW_KEY_SPACE && action == GLFW_PRESS) {
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

            if(mode == Mode.EDIT && !chatbox.focus) {
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

            animationManager.update();
            worldRenderer.update();

            try {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.clear();
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    int length, total = 0;
                    do {
                        length = socketChannel.read(buffer);
                        total += length;
                        bos.write(buffer.array(), 0, length);
                        buffer.clear();
                    } while (length > 0);
                    if (total > 0) {
                        System.out.println(total);
                        try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
                            ObjectInputStream ois = new ObjectInputStream(bis);
                            ((ClientPayload) ois.readObject()).execute(this.consRes);
                        }
                        catch (ClassNotFoundException e) {
                            System.err.println("ServerPayload: invalid type");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // all updates go here

            glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer

            // everything drawing goes here
            camera.updateView();

            if(selectedID == -1) {
                worldRenderer.setMouseWorldPosition(new Vector2i(mouseWorldPosition));
            } else {
                GameObject selectedObject = world.gameObjects.get(selectedID);
                worldRenderer.setMouseWorldPosition(new Vector2i(selectedObject.x, selectedObject.y));
            }
            worldRenderer.draw(camera);

            chatbox.draw(camera.getProjection());

//            for(GameObject gameObject : world.gameObjects.values()) {
//                ClickBoxManager.ClickBox box = clickBoxManager.getGameObjectClickBox(gameObject.uniqueID);
//                if(box != null) {
//                    boxRenderer.draw(new Matrix4f(camera.getProjView()).translate(box.center().x, box.center().y, 0).scale(box.scale().x, box.scale().y, 0),
//                        new Vector4f(0.5f));
//                }
//            }
//            boxRenderer.draw(new Matrix4f(camera.getProjView()).translate(mouseViewPosition.x, mouseViewPosition.y, 0).scale(0.25f),
//                new Vector4f(0.5f));

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