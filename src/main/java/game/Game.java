package game;

import model.abilities.AbilityComponent;
import model.abilities.MoveAbility;
import model.abilities.MoveAction;
import model.grid.ByteGrid;
import model.grid.Pathfinding;
import model.grid.TileGrid;
import network.ClientConnection;
import network.ServerPayload;
import org.joml.*;
import org.json.JSONObject;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import render.*;
import server.*;
import network.commands.*;
import model.*;
import util.FileUtil;
import util.MathUtil;

import java.io.IOException;
import java.lang.Math;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.Random;

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
    public int screenWidth = 800, screenHeight = 600;
    public final Vector2f mousePosition = new Vector2f();
    public final Vector2i mouseWorldPosition = new Vector2i();
    public final Vector2f mouseViewPosition = new Vector2f();

    public GameTime gameTime;
    public Camera camera;
    public Chatbox chatbox;

    public World world;
    public WorldRenderer worldRenderer;
    public AnimationManager animationManager;
    public ClickBoxManager clickBoxManager;
    public SelectGridManager selectGridManager;

    public GameData gameData;
    public VisualData visualData;

    public ClientConnection<ClientPayload, ServerPayload> connection;
    public ClientInfo clientInfo;

    private Mode mode = Mode.PLAY;
    public ActionArranger currentCommand = null;
    private KeyMapping keyMapping = new KeyMapping();

    private final Map<ResourceID, Texture> resourceTexture = new HashMap<>();

    private List<Integer> editorObjectPlacementKeys = Arrays.asList(
            GLFW_KEY_1, GLFW_KEY_2, GLFW_KEY_3, GLFW_KEY_4, GLFW_KEY_5,
            GLFW_KEY_6, GLFW_KEY_7, GLFW_KEY_8, GLFW_KEY_9, GLFW_KEY_0,
            GLFW_KEY_MINUS, GLFW_KEY_EQUAL, GLFW_KEY_Q, GLFW_KEY_W,
            GLFW_KEY_E, GLFW_KEY_R, GLFW_KEY_T, GLFW_KEY_Y,
            GLFW_KEY_U, GLFW_KEY_I, GLFW_KEY_O, GLFW_KEY_P
    );

    enum Mode {
        PLAY, EDIT
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
        this.gameTime = new GameTime(window);
        this.chatbox = new Chatbox(font, boxRenderer, gameTime);
        this.camera = new Camera(gameTime, window);

        this.gameData = new GameData();
        this.visualData = new VisualData();
        try {
            String file = FileUtil.readFile("gamedata.json");
            this.gameData.fromJSON(new JSONObject(file), e -> {
                chatbox.println("Failed to parse JSON game data");
                chatbox.println(e.getMessage());
                e.printStackTrace();
            });
        } catch(IOException e) {
            chatbox.println("JSON file missing (probably)");
            e.printStackTrace();
        }
        try {
            String file = FileUtil.readFile("visualdata.json");
            this.visualData.fromJSON(new JSONObject(file), e -> {
                chatbox.println("Failed to parse JSON game data");
                chatbox.println(e.getMessage());
                e.printStackTrace();
            });
            for(ResourceID resourceID : visualData.getLoadedResources()) {
                this.resourceTexture.put(resourceID, Texture.makeTexture(visualData.getResourceType(resourceID).getDisplay().getTexture()));
            }
        } catch(IOException e) {
            chatbox.println("JSON file missing (probably)");
            e.printStackTrace();
        }
        this.world = new World();
        this.worldRenderer = new WorldRenderer(camera, gameData, visualData, world, gameTime);
        this.animationManager = new AnimationManager();

        this.clickBoxManager = new ClickBoxManager(world, visualData, camera, worldRenderer);
        this.clientInfo = new ClientInfo();
        this.clientInfo.clientID = ClientID.getPlaceholder();

        this.connection = new ClientConnection<>();
        this.connection.setOnConnectHandler(socketAddress -> {
            connection.queueSend(new GetWorld());
            connection.queueSend(new GetClientID());
        });
        this.connection.connect(new InetSocketAddress("24.247.145.133", Server.PORT));
        this.connection.queueSend(new EchoPayload("Connection succeeded"));

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
                if(currentCommand == null) {
                    GameObject selectedObject = clickBoxManager.getGameObjectAtViewPosition(mouseViewPosition, world.teams.getClientTeam(clientInfo.clientID));
                    if(selectedObject != null && selectedObject.team.equals(world.teams.getClientTeam(clientInfo.clientID))) {
                        clickBoxManager.selectedID = selectedObject.uniqueID;
                    } else {
                        clickBoxManager.selectedID = null;
                    }
                } else {
                    if(!animationManager.isObjectOccupied(clickBoxManager.selectedID)) {
                        Action commandAction = currentCommand.createAction(this);
                        if(commandAction != null && commandAction.validate(clientInfo.clientID, world, gameData)) {
                            connection.queueSend(new ActionCommand(commandAction, world));
                            runAction(commandAction);
                        }
                        currentCommand.clearArrangement(this);
                        currentCommand = null;
                    }
                }
            }
        } else if(mode == Mode.EDIT) {
            if(button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS && glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
                ByteGrid grid = world.grid.makeTileGrid(mouseWorldPosition.x, mouseWorldPosition.y);
                for(int i = 0; i < ByteGrid.SIZE; ++i) {
                    for(int j = 0; j < ByteGrid.SIZE; ++j) {
                        if(grid.get(i, j) == 0) grid.set((byte) 1, i, j);
                    }
                }
                worldRenderer.tileGridRenderer.build(grid);
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
            if(key == GLFW_KEY_UP && action == GLFW_PRESS) {
                chatbox.prevCommand();
            }
            else if(key == GLFW_KEY_DOWN && action == GLFW_PRESS) {
                chatbox.nextCommand();
            }
        } else {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                currentCommand.clearArrangement(this);
                currentCommand = null;
            }
            if(key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                chatbox.enable();
            } else if(mode == Mode.PLAY) {
                if(world.teams.isClientsTurn(clientInfo.clientID)) {
                    if(currentCommand == null) {
                        Integer slot = keyMapping.getKeyAbilitySlot(key);
                        if(slot != null && action == GLFW_PRESS) {
                            GameObject obj = world.gameObjects.get(clickBoxManager.selectedID);
                            AbilityComponent abilityComponent = null;
                            ActionArranger arranger = null;
                            if(obj != null) abilityComponent = gameData.getType(obj.type).getAbility(slot);
                            if(abilityComponent != null) arranger = AbilityOrganizer.abilityActionArranger.get(abilityComponent.getTypeID()).get();
                            if(arranger != null && arranger.arrange(this, slot)) currentCommand = arranger;
                        }
                    }
                    if(key == GLFW_KEY_SPACE && action == GLFW_PRESS) {
                        if(connection.isConnected()) {
                            connection.queueSend(new EndTurn());
                        } else {
                            world.teams.endClientTurn(clientInfo.clientID);
                            if(world.teams.teamEndedTurn(world.teams.getClientTeam(clientInfo.clientID))) {
                                NextTurn.executeNextTurn(world, gameData, s -> chatbox.println(s), s -> chatbox.println(s));
                                TeamID teamID = world.teams.getTurn();
                                world.teams.setClientTeam(clientInfo.clientID, teamID);
                                chatbox.println("Local team joined: " + teamID + ", name: " + world.teams.getTeamName(teamID));
                            }
                        }
                        currentCommand = null;
                        worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>());
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
                payload.execute(this);
            }

            // Poll for window events. Invokes window callbacks
            pollMousePosition();
            glfwPollEvents();
            if(!chatbox.focus) camera.move();

            if(mode == Mode.EDIT && !chatbox.focus) {
                if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
                    if(world.grid.getTile(mouseWorldPosition.x, mouseWorldPosition.y) != 2) {
                        ByteGrid updateGrid = world.grid.setTile((byte) 2, mouseWorldPosition.x, mouseWorldPosition.y);
                        worldRenderer.tileGridRenderer.build(updateGrid);
                    }
                } else if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
                    if(world.grid.getTile(mouseWorldPosition.x, mouseWorldPosition.y) != 1) {
                        ByteGrid updateGrid = world.grid.setTile((byte) 1, mouseWorldPosition.x, mouseWorldPosition.y);
                        worldRenderer.tileGridRenderer.build(updateGrid);
                    }
                }
                for(int i = 0; i < editorObjectPlacementKeys.size(); ++i) {
                    if(glfwGetKey(window, editorObjectPlacementKeys.get(i)) == GLFW_PRESS) {
                        if(i >= gameData.getTypes().size()) {
                            chatbox.println("Missing entity type");
                        } else {
                            GameObjectType type = gameData.getTypes().get(i);
                            ClientID clientID = clientInfo.clientID;
                            TeamID team = world.teams.getClientTeam(clientID);
                            if(!type.isNeutral() && team == null) chatbox.println("Need to have team");
                            Set<Vector2i> tiles = MathUtil.addToAll(type.getRelativeOccupiedTiles(), mouseWorldPosition);
                            boolean canPlace = true;
                            for(Vector2i tile : tiles) {
                                if(!world.occupied(tile.x, tile.y, gameData).isEmpty()) {
                                    canPlace = false;
                                }
                            }
                            if(canPlace) {
                                GameObject obj = world.gameObjectFactory.createGameObject(type, team, gameData);
                                if(obj != null) {
                                    obj.x = mouseWorldPosition.x;
                                    obj.y = mouseWorldPosition.y;
                                    if(world.add(obj, gameData)) {
                                        worldRenderer.resetGameObjectRenderCache();
                                        clickBoxManager.resetGameObjectCache();
                                    } else {
                                        // Should never happen since the routine above to check "canPlace"
                                        // is identical to what world.add does
                                        // TODO: optimize world.add to not check if something else is already there
                                        System.err.println("Failed to place object: " + obj.uniqueID);
                                    }
                                }
                            }
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
//                                    chatbox.println("Must be connected");
                                    String targetTeam = args[1];
                                    if(targetTeam == null || targetTeam.length() <= 1) {
                                        chatbox.println("Invalid args");
                                    } else {
                                        TeamID teamID = world.teams.getTeamWithName(targetTeam);
                                        if(teamID == null) {
                                            teamID = world.teams.teamIDGenerator.generate();
                                            world.teams.addTeam(teamID);
                                            world.teams.setTeamName(teamID, targetTeam);
                                        }
                                        world.teams.setClientTeam(clientInfo.clientID, teamID);
                                        chatbox.println("Local team joined: " + teamID + ", name: " + targetTeam);
                                        NextTurn.executeNextTurn(world, gameData, s -> chatbox.println(s), s -> chatbox.println(s));
                                    }
                                }
                            }
                        } else if(args[0].equals("teams")) {
                            if(connection.isConnected()) {
                                connection.queueSend(new ListTeams());
                            } else {
                                StringBuilder str = new StringBuilder();
                                for(TeamID id : world.teams.getTeams()) {
                                    str.append(world.teams.getTeamName(id)).append(", ");
                                }
                                chatbox.println(str.toString());
                            }
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
                        } else if(args[0].equals("nextturn")) {
                            if(connection.isConnected()) {
                                connection.queueSend(new NextTurn());
                            } else {
                                NextTurn.executeNextTurn(world, gameData, s -> chatbox.println(s), s -> chatbox.println(s));
                            }
                        } else if(args[0].equals("endturn")) {
                            if(connection.isConnected()) {
                                connection.queueSend(new EndTurn());
                            } else {
                                world.teams.endClientTurn(clientInfo.clientID);
                                if(world.teams.teamEndedTurn(world.teams.getClientTeam(clientInfo.clientID))) {
                                    NextTurn.executeNextTurn(world, gameData, s -> chatbox.println(s), s -> chatbox.println(s));
                                }
                            }
                        } else if(args[0].equals("dead")) {
                            connection.queueSend(new DeadCommand());
                        } else if(args[0].equals("gamedata")) {
                            if(args.length < 2) {
                                chatbox.println("Invalid number of arguments");
                            } else if(args[1].equalsIgnoreCase("send")) {
                                connection.queueSend(new SetGameData(gameData));
                            } else if(args[1].equalsIgnoreCase("save")) {
                                FileUtil.writeFile(String.join(" ", Arrays.copyOfRange(args, 2, args.length)), gameData.toJSON().toString(4));
                            } else if(args[1].equalsIgnoreCase("load")) {
                                if(args.length < 3) {
                                    chatbox.println("Need file name to load");
                                } else {
                                    String path = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                                    String data = FileUtil.readFile(path);
                                    JSONObject obj = new JSONObject(data);
                                    if(gameData.fromJSON(obj, e -> {
                                        chatbox.println("Failed to load JSON file " + path);
                                        chatbox.println(e.getMessage());
                                        e.printStackTrace();
                                    })) {
                                        chatbox.println("Game data loaded on client");
                                    }
                                }
                            }
                        } else if(args[0].equals("visualdata")) {
                            if(args.length < 2) {
                                chatbox.println("Invalid number of arguments");
                            } else if(args[1].equalsIgnoreCase("send")) {
                                //connection.queueSend(new SetGameData(gameData));
                            } else if(args[1].equalsIgnoreCase("save")) {
                                FileUtil.writeFile(String.join(" ", Arrays.copyOfRange(args, 2, args.length)), visualData.toJSON().toString(4));
                            } else if(args[1].equalsIgnoreCase("load")) {
                                if(args.length < 3) {
                                    chatbox.println("Need file name to load");
                                } else {
                                    String path = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                                    String data = FileUtil.readFile(path);
                                    JSONObject obj = new JSONObject(data);
                                    if(visualData.fromJSON(obj, e -> {
                                        chatbox.println("Failed to load JSON file " + path);
                                        chatbox.println(e.getMessage());
                                        e.printStackTrace();
                                    })) {
                                        chatbox.println("Visual data loaded");
                                        clickBoxManager.resetGameObjectCache();
                                        worldRenderer.resetGameObjectRenderCache();
                                        animationManager.reset();
                                        worldRenderer.rebuildTerrain();
                                        clickBoxManager.selectedID = null;
                                        for(ResourceID resourceID : visualData.getLoadedResources()) {
                                            this.resourceTexture.put(resourceID, Texture.makeTexture(visualData.getResourceType(resourceID).getDisplay().getTexture()));
                                        }
                                    }
                                }
                            }
                        } else if(args[0].equals("moveall")) {
                            TeamID team = world.teams.getClientTeam(clientInfo.clientID);
                            if(team != null) {
                                TileGrid grid = new TileGrid(gameData, world);
                                for(GameObject gameObject : world.gameObjects.values()) {
                                    if(gameObject.team.equals(team)) {
                                        Collection<Vector2i> targets = Pathfinding.pathPossibilities(grid, gameObject.uniqueID, new Vector2i(gameObject.x, gameObject.y), gameObject.speedLeft).possibilities();
                                        int n = new Random().nextInt(targets.size());
                                        Vector2i target = new Vector2i();
                                        for(Vector2i v : targets) {
                                            if(n-- == 0) {
                                                target = v;
                                                break;
                                            }
                                        }
                                        GameObjectType type = gameData.getType(gameObject.type);
                                        AbilityID id = type.getFirstAbilityWithType(MoveAbility.class);
                                        if(id != null) {
                                            MoveAction action = new MoveAction(id, gameObject.uniqueID, target.x, target.y);
                                            if(action.validate(clientInfo.clientID, world, gameData)) {
                                                runAction(action);
                                                connection.queueSend(new ActionCommand(action, world));
                                            }
                                        }
                                    }
                                }
                            }
                        } else if(args[0].equals("save")) {
                            if(args.length < 2) {
                                chatbox.println("Requires file name");
                            } else {
                                String fileName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                                JSONObject obj = world.toInitJSON();
                                FileUtil.writeFile(fileName, obj.toString(4));
                            }
                        } else if(args[0].equals("load")) {
                            if(args.length < 2) {
                                chatbox.println("Requires file name");
                            } else {
                                String fileName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                                JSONObject obj = new JSONObject(FileUtil.readFile(fileName));
                                if(connection.isConnected()) {
                                    connection.queueSend(new SetWorldJSON(obj.toString()));
                                } else {
                                    world.initFromJSON(obj, gameData);
                                    chatbox.println("Successfully reinitialized world");
                                }
                                clickBoxManager.resetGameObjectCache();
                                worldRenderer.resetGameObjectRenderCache();
                                animationManager.reset();
                                worldRenderer.rebuildTerrain();
                                clickBoxManager.selectedID = null;
                            }
                        } else if(args[0].equals("teamname")) {
                            if(connection.isConnected() && args.length == 2) {
                                connection.queueSend(new SetTeamName(args[1]));
                            } else {
                                chatbox.println("Invalid command/arguments");
                            }
                        } else if(args[0].equals("setres")) {
                            if(args.length != 3 && args.length != 4) {
                                chatbox.println("Invalid arguments");
                            } else {
                                TeamID teamID = world.teams.getClientTeam(clientInfo.clientID);
                                ResourceID resourceID;
                                int amount;
                                if(args.length == 3) {
                                    resourceID = gameData.getResourceID(args[1]);
                                    amount = Integer.parseInt(args[2]);
                                } else {
                                    teamID = world.teams.getTeamWithName(args[1]);
                                    resourceID = gameData.getResourceID(args[2]);
                                    amount = Integer.parseInt(args[3]);
                                }
                                if(teamID == null || resourceID == null || amount < 0) {
                                    chatbox.println("Invalid arguments");
                                } else {
                                    SetResources setResources = new SetResources(teamID, resourceID, amount);
                                    setResources.execute(this);
                                    if(connection.isConnected()) {
                                        connection.queueSend(setResources);
                                    }
                                }
                            }
                        } else {
                            chatbox.println("Unknown command!");
                        }
                    } else {
                        connection.queueSend(new ChatMessage(cmd));
                    }
                } catch(Exception e) {
                    chatbox.println("Error processing command:");
                    chatbox.println(e.getMessage());
                    e.printStackTrace();
                }
            }
            chatbox.prevCommands.addAll(chatbox.commands);
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
                Set<Vector2i> occupied = MathUtil.addToAll(
                        gameData.getType(selectedObject.type).getRelativeOccupiedTiles(),
                        new Vector2i(selectedObject.x, selectedObject.y));

                if(currentCommand != null) {
                    currentCommand.changeMouseSelection(this, occupied);
                }

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

            float rightMargin = 8;
            float offsetX = 0;
            float resSize = 32;
            float textWidth = font.textWidth(" 00000");
            float totalWidth = (textWidth + resSize + rightMargin) * gameData.getResourceIDs().size();
            Matrix4f background = new Matrix4f(camera.getProjection());
            background.translate(screenWidth - totalWidth / 2.0f, font.getSize() / 2.0f + 4, 0);
            background.scale(totalWidth, font.getSize() + 8, 0);
            boxRenderer.draw(background, new Vector4f(0, 0, 0, 0.4f));
            for(ResourceID resourceID : gameData.getResourceIDs()) {
                // get resource visual
                VisualDataResourceType type = visualData.getResourceType(resourceID);
                ResourceDisplay display = type.getDisplay();
                Texture texture = resourceTexture.get(resourceID);
                texture.bind();
                Matrix4f mat4 = new Matrix4f(camera.getProjection());
                mat4.translate(screenWidth - textWidth - resSize / 2.0f - offsetX - rightMargin, resSize / 2.0f, 0);
                mat4.scale(resSize);
                mat4.mul(MathUtil.getDisplayMatrix(display.getSizeMultiplier(), display.getOffset(), texture.size));
                textureRenderer.draw(mat4, new Vector4f(1));

                // draw text
                String text = ""+world.teams.getTeamResource(world.teams.getClientTeam(clientInfo.clientID), resourceID);
                float currTextWidth = font.textWidth(text);
                this.font.draw(text,
                        screenWidth - currTextWidth - offsetX - rightMargin,
                        font.getSize(),
                        new Matrix4f(camera.getProjection()));

                offsetX += textWidth + resSize + rightMargin;
            }

            glfwSwapBuffers(window); // swap the color buffers, rendering what was drawn to the screen
        }

        Shaders.checkGLError("End main loop");
    }

    public void cleanUp() {
        boxRenderer.cleanUp();
        textureRenderer.cleanUp();
        connection.close();
    }

    /** Executes and animates an Action, if possible */
    public void runAction(Action action) {
        AnimatorSupplier supplier;
        Animator animator = null;

        supplier = AbilityOrganizer.abilityAnimatorSupplier.get(action.getID());
        if(supplier != null) animator = supplier.get(action);
        if(animator != null) animator.animate(this);
        else {
            chatbox.println("Failed to find animator for action with ability ID: " + action.getID());
            action.execute(world, gameData);
        }
    }

    public static void main(String[] args) throws Exception {
        new Game().run();
    }

}