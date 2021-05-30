package game;

import org.joml.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import render.*;
import server.*;
import server.commands.*;
import staticData.GameData;
import staticData.GameObjectType;

import java.io.*;
import java.lang.Math;
import java.net.InetSocketAddress;
import java.util.*;

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

    private GameTime gameTime;
    private Camera camera;
    private Chatbox chatbox;

    private World world;
    private WorldRenderer worldRenderer;
    private AnimationManager animationManager;
    private ClickBoxManager clickBoxManager;
    private SelectGridManager selectGridManager;

    private GameData gameData;
    private GameObjectFactory gameObjectFactory;

    private ClientConnection<ClientPayload, ServerPayload> connection;
    private ClientInfo clientInfo;

    private GameResources consRes;

    private Mode mode = Mode.PLAY;
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
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

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
        this.font = new Font("font.ttf", 24, 512, 512);

        this.gameData = new GameData();
        this.world = new World();
        this.worldRenderer = new WorldRenderer(camera, gameData, world, gameTime);
        this.animationManager = new AnimationManager();

        this.gameTime = new GameTime(window);
        this.chatbox = new Chatbox(font, boxRenderer, gameTime);
        this.camera = new Camera(gameTime, window);
        this.clickBoxManager = new ClickBoxManager(world, gameData, camera, worldRenderer);
        this.gameObjectFactory = new GameObjectFactory();
        this.clientInfo = new ClientInfo();

        this.connection = new ClientConnection<>();
        this.connection.setOnConnectHandler(socketAddress -> {
            connection.queueSend(new GetWorld());
            connection.queueSend(new GetClientID());
        });
        this.connection.connect(new InetSocketAddress("localhost", Server.PORT));
        this.connection.queueSend(new EchoPayload("Connection succeeded"));

        this.consRes = new GameResources(camera, chatbox, gameObjectFactory, world, worldRenderer, animationManager, clickBoxManager, selectGridManager, gameData, gameTime, connection, clientInfo);

        this.selectGridManager = new SelectGridManager(world, gameData);

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
        Vector2f vpos = camera.screenToViewSpace(mousePosition);
        Vector2f pos = Camera.viewToWorldSpace(vpos);
        mouseWorldPosition.set(MathUtil.floor(pos.x), MathUtil.floor(pos.y));
        mouseViewPosition.set(vpos);
    }

    private void mouseButton(int button, int action, int mods) {
        if(mode == Mode.PLAY) {
            if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                if(currentCommand == UICommand.NONE) {
                    GameObject selectedObject = clickBoxManager.getGameObjectAtViewPosition(mouseViewPosition, world.teams.getClientTeam(clientInfo.clientID));
                    if(selectedObject != null && selectedObject.team.equals(world.teams.getClientTeam(clientInfo.clientID))) {
                        clickBoxManager.selectedID = selectedObject.uniqueID;
                    } else {
                        clickBoxManager.selectedID = null;
                    }
                } else {
                    if(!animationManager.isObjectOccupied(clickBoxManager.selectedID)) {
                        if (currentCommand == UICommand.MOVE) {
                            MoveAction moveAction = new MoveAction(clickBoxManager.selectedID, mouseWorldPosition.x, mouseWorldPosition.y);
                            if(moveAction.validate(clientInfo.clientID, world, gameData)) {
                                connection.queueSend(new ActionCommand(moveAction, world));
                                moveAction.animate(consRes);
                            }
                            currentCommand = UICommand.NONE;
                            worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>());
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
                worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>());
            }
            if(key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                chatbox.enable();
            } else if(mode == Mode.PLAY) {
                if (currentCommand == UICommand.NONE && !animationManager.isObjectOccupied(clickBoxManager.selectedID)) { // no command is currently selected
                    if(clickBoxManager.selectedID != null &&
                            world.gameObjects.get(clickBoxManager.selectedID) != null && gameData.getType(world.gameObjects.get(clickBoxManager.selectedID).type).getBaseSpeed() > 0 &&
                            key == GLFW_KEY_Q && action == GLFW_PRESS) {
                        currentCommand = UICommand.MOVE;
                        selectGridManager.regenerateSelect(clickBoxManager.selectedID);
                        worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>(selectGridManager.getSelectionGrid().map.values()));
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

            Collection<ClientPayload> payloads = connection.update();
            for(ClientPayload payload : payloads) {
                payload.execute(this.consRes);
            }

            // Poll for window events. Invokes window callbacks
            pollMousePosition();
            glfwPollEvents();
            camera.move();

            if(mode == Mode.EDIT && !chatbox.focus) {
                if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
                    if (world.grid.getTile(mouseWorldPosition.x, mouseWorldPosition.y) != 2) {
                        ByteGrid updateGrid = world.grid.setTile((byte) 2, mouseWorldPosition.x, mouseWorldPosition.y);
                        worldRenderer.tileGridRenderer.build(updateGrid);
                    }
                } else if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
                    if (world.grid.getTile(mouseWorldPosition.x, mouseWorldPosition.y) != 1) {
                        ByteGrid updateGrid = world.grid.setTile((byte) 1, mouseWorldPosition.x, mouseWorldPosition.y);
                        worldRenderer.tileGridRenderer.build(updateGrid);
                    }
                } else if (glfwGetKey(window, GLFW_KEY_1) == GLFW_PRESS) {
                    ClientID clientID = clientInfo.clientID;
                    TeamID team = world.teams.getClientTeam(clientID);
                    GameObject obj = gameObjectFactory.createGameObject(gameData.getPlaceholder(), team);
                    if(obj != null) {
                        obj.x = mouseWorldPosition.x;
                        obj.y = mouseWorldPosition.y;
                        if(world.add(obj, gameData)) {
                            worldRenderer.resetGameObjectRenderCache();
                            clickBoxManager.resetGameObjectCache();
                        }
                    }
                } else if (glfwGetKey(window, GLFW_KEY_2) == GLFW_PRESS) {
                    ClientID clientID = clientInfo.clientID;
                    TeamID team = world.teams.getClientTeam(clientID);
                    GameObject obj = gameObjectFactory.createGameObject(gameData.getBuildingPlaceholder(), team);
                    if(obj != null) {
                        obj.x = mouseWorldPosition.x;
                        obj.y = mouseWorldPosition.y;
                        if(world.add(obj, gameData)) {
                            worldRenderer.resetGameObjectRenderCache();
                            clickBoxManager.resetGameObjectCache();
                        }
                    }
                }
            } else if(mode == Mode.PLAY) {
            }

            chatbox.update();
            for(String cmd : chatbox.commands) {
                try {
                    if(cmd.startsWith("/")) {
                        String[] args = cmd.substring(1).split("\\s");
                        args[0] = args[0].toLowerCase();
                        if(args[0].equals("test")) {
                            chatbox.println("Testing!");
                        } else if(args[0].equals("edit")) {
                            mode = Mode.EDIT;
                            chatbox.println("Editing enabled");
                        } else if(args[0].equals("play")) {
                            mode = Mode.PLAY;
                            chatbox.println("Gameplay enabled");
                        } else if(args[0].equals("connect")) {
                            if(args.length == 2) {
                                connection.connect(new InetSocketAddress(args[1], Server.PORT));
                                chatbox.println("Attempting to connect to " + args[1] + ":" + Server.PORT);
                                connection.queueSend(new EchoPayload("Connection complete"));
                            } else {
                                chatbox.println("Need 2 params");
                            }
                        } else if(args[0].equals("echo")) {
                            if(connection.isConnected()) {
                                connection.queueSend(new EchoPayload("Echo: " + cmd.substring(1 + args[0].length())));
                            }
                        } else if(args[0].equals("getworld")) {
                            if(connection.isConnected()) {
                                connection.queueSend(new GetWorld());
                            }
                        } else if(args[0].equals("name")) {
                            if(args.length != 2) {
                                chatbox.println("Must use 2 arguments");
                            } else {
                                if(connection.isConnected()) {
                                    connection.queueSend(new NameChange(args[1]));
                                }
                            }
                        } else if(args[0].equals("setworld")) {
                            if(connection.isConnected()) {
                                connection.queueSend(new SetWorld(world));
                            }
                        } else if(args[0].equals("join")) {
                            if(args.length != 2 || args[1].length() <= 1) {
                                chatbox.println("Must use 2 arguments");
                            } else {
                                if(connection.isConnected()) {
                                    connection.queueSend(new JoinTeam(args[1]));
                                } else {
                                    chatbox.println("Must be connected");
                                }
                            }
                        } else if(args[0].equals("teams")) {
                            connection.queueSend(new ListTeams());
                        } else if(args[0].equals("delteam")) {
                            if(args.length != 2 || args[1].length() <= 1) {
                                chatbox.println("Must use 2 arguments");
                            } else {
                                connection.queueSend(new RemoveTeam(world.teams.getTeamWithName(args[1])));
                            }
                        } else if(args[0].equals("getteamcolor")) {
                            if(args.length != 2 || args[1].length() <= 1) {
                                chatbox.println("Must use 2 arguments");
                            } else {
                                TeamID team = world.teams.getTeamWithName(args[1]);
                                String teamName = world.teams.getTeamName(team);
                                if(team == null) {
                                    chatbox.println("Team not found.");
                                } else {
                                    Vector3f color = world.teams.getTeamColor(team);
                                    if(color == null) chatbox.println("Team " + teamName + " color undefined.");
                                    else chatbox.println("Team " + teamName +
                                            " color: Red=" + (int) (color.x * 255) +
                                            " Green=" + (int) (color.y * 255) +
                                            " Blue=" + (int) (color.z * 255));
                                }
                            }
                        } else if(args[0].equals("setteamcolor")) {
                            if(args.length != 5 || args[1].length() <= 1) {
                                chatbox.println("Must use 5 arguments");
                            } else {
                                String teamName = args[1];
                                Vector3f color = new Vector3f(
                                        Math.min(255, Math.max(0, Integer.parseInt(args[2]))) / 255.0f,
                                        Math.min(255, Math.max(0, Integer.parseInt(args[3]))) / 255.0f,
                                        Math.min(255, Math.max(0, Integer.parseInt(args[4]))) / 255.0f
                                );
                                connection.queueSend(new SetTeamColor(teamName, color));
                            }
                        } else {
                            chatbox.println("Unknown command!");
                        }
                    } else {
                        connection.queueSend(new ChatMessage(cmd));
                    }
                } catch(Exception e) {
                    System.err.println("Error processing command");
                    e.printStackTrace();
                }
            }
            chatbox.commands.clear();

            animationManager.update();

            worldRenderer.update();

            // all updates go here

            glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer

            // everything drawing goes here
            camera.updateView();

            if(clickBoxManager.selectedID == null) {
                worldRenderer.setMouseWorldPosition(Collections.singletonList(new Vector2i(mouseWorldPosition)));
            } else {
                GameObject selectedObject = world.gameObjects.get(clickBoxManager.selectedID);
                Collection<Vector2i> occupied = MathUtil.addToAll(gameData.getType(selectedObject.type).getRelativeOccupiedTiles(), new Vector2i(selectedObject.x, selectedObject.y));
                worldRenderer.setMouseWorldPosition(occupied);
            }
            worldRenderer.draw(camera);

//            for(ClickBoxManager.ClickBox clickBox : clickBoxManager.clickBoxes) {
//                boxRenderer.draw(new Matrix4f(camera.getProjView()).translate(clickBox.center().x, clickBox.center().y, 0).scale(clickBox.scale().x, clickBox.scale().y, 1), new Vector4f(1, 1, 1, 0.6f));
//            }

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
        connection.close();
    }

    public static void main(String[] args) throws Exception {
//        TeamID.Generator gen = new TeamID.Generator();
//        ClientID.Generator gen2 = new ClientID.Generator();
//        Map<TeamID, Integer> test = new HashMap<>();
//        TeamID id = gen.generate();
//        ClientID client = gen2.generate();
//        test.put(id, 5);
//        System.out.println(test.get(id));
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ObjectOutputStream oos = new ObjectOutputStream(baos);
//        oos.writeObject(id);
//        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
//        TeamID chadID = (TeamID) ois.readObject();
//
//        TeamManager manager = new TeamManager();
//        manager.addTeam(id);
//        System.out.println(manager.getClientTeam(client));
//        manager.setClientTeam(client, id);
//        System.out.println(manager.getClientTeam(client));
//        manager.setClientTeam(client, chadID);
//        System.out.println(manager.getClientTeam(client));
//        System.out.println(manager.getTeamClients(chadID));
//        System.out.println(manager.getTeamClients(id));
//
//        System.exit(0);
        new Game().run();
    }

}