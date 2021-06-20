package game;

import org.joml.*;
import org.json.JSONObject;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import render.*;
import server.*;
import server.commands.*;
import staticData.GameData;

import java.io.IOException;
import java.io.InputStream;
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
    private int screenWidth = 800, screenHeight = 600;
    private final Vector2f mousePosition = new Vector2f();
    private final Vector2i mouseWorldPosition = new Vector2i();
    private final Vector2f mouseViewPosition = new Vector2f();

    public GameTime gameTime;
    public Camera camera;
    public Chatbox chatbox;

    public World world;
    public WorldRenderer worldRenderer;
    public AnimationManager animationManager;
    public ClickBoxManager clickBoxManager;
    public SelectGridManager selectGridManager;

    public GameData gameData;

    public ClientConnection<ClientPayload, ServerPayload> connection;
    public ClientInfo clientInfo;

    private Mode mode = Mode.PLAY;
    private UICommand currentCommand = UICommand.NONE;

    enum Mode {
        PLAY, EDIT
    }
    enum UICommand {
        NONE, MOVE, SPAWN, ATTACK, GROW
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
        try {
            String file = MathUtil.readFile("gamedata.json");
            this.gameData.fromJSON(new JSONObject(file), e -> {
                chatbox.println("Failed to parse JSON game data");
                chatbox.println(e.getMessage());
            });
        } catch(IOException e) {
            chatbox.println("JSON file missing (probably)");
            e.printStackTrace();
        }
        this.world = new World();
        this.worldRenderer = new WorldRenderer(camera, gameData, world, gameTime);
        this.animationManager = new AnimationManager();

        this.clickBoxManager = new ClickBoxManager(world, gameData, camera, worldRenderer);
        this.clientInfo = new ClientInfo();
        this.clientInfo.clientID = ClientID.getPlaceholder();

        this.connection = new ClientConnection<>();
        this.connection.setOnConnectHandler(socketAddress -> {
            connection.queueSend(new GetWorld());
            connection.queueSend(new GetClientID());
        });
        this.connection.connect(new InetSocketAddress("", Server.PORT));
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
                if(currentCommand == UICommand.NONE) {
                    GameObject selectedObject = clickBoxManager.getGameObjectAtViewPosition(mouseViewPosition, world.teams.getClientTeam(clientInfo.clientID));
                    if(selectedObject != null && selectedObject.team.equals(world.teams.getClientTeam(clientInfo.clientID))) {
                        clickBoxManager.selectedID = selectedObject.uniqueID;
                    } else {
                        clickBoxManager.selectedID = null;
                    }
                } else if(currentCommand == UICommand.MOVE) {
                    if(!animationManager.isObjectOccupied(clickBoxManager.selectedID)) {
                        MoveAction moveAction = new MoveAction(clickBoxManager.selectedID, mouseWorldPosition.x, mouseWorldPosition.y);
                        if(moveAction.validate(clientInfo.clientID, world, gameData)) {
                            connection.queueSend(new ActionCommand(moveAction, world));
                            moveAction.animate(this);
                        }
                        currentCommand = UICommand.NONE;
                        worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>());
                    }
                } else if(currentCommand == UICommand.SPAWN) {
                    if(!animationManager.isObjectOccupied(clickBoxManager.selectedID)) {
                        SpawnAction spawnAction = new SpawnAction(clickBoxManager.selectedID, mouseWorldPosition.x, mouseWorldPosition.y);
                        if(spawnAction.validate(clientInfo.clientID, world, gameData)) {
                            connection.queueSend(new ActionCommand(spawnAction, world));
                            spawnAction.animate(this);
                        }
                        currentCommand = UICommand.NONE;
                        worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>());
                    }
                } else if(currentCommand == UICommand.ATTACK) {
                    GameObject selectedObject = clickBoxManager.getGameObjectAtViewPositionExcludeTeam(mouseViewPosition, world.teams.getClientTeam(clientInfo.clientID));
                    if(selectedObject != null && !selectedObject.team.equals(world.teams.getClientTeam(clientInfo.clientID)) && !animationManager.isObjectOccupied(selectedObject.uniqueID)) {
                        AttackAction attackAction = new AttackAction(clickBoxManager.selectedID, selectedObject.uniqueID);
                        if(attackAction.validate(clientInfo.clientID, world, gameData)) {
                            connection.queueSend(new ActionCommand(attackAction, world));
                            attackAction.animate(this);
                        }
                        currentCommand = UICommand.NONE;
                        worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>());
                    } else {
                        currentCommand = UICommand.NONE;
                        worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>());
                    }
                } else if(currentCommand == UICommand.GROW) {
                    GameObject selectedObject = world.gameObjects.get(clickBoxManager.selectedID);
                    if(selectedObject != null) {
                        ArrayList<GameObjectID> seeds = new ArrayList<>();
                        seeds.add(world.occupied(mouseWorldPosition.x, mouseWorldPosition.y, gameData));
                        seeds.add(world.occupied(mouseWorldPosition.x + 1, mouseWorldPosition.y, gameData));
                        seeds.add(world.occupied(mouseWorldPosition.x + 1, mouseWorldPosition.y + 1, gameData));
                        seeds.add(world.occupied(mouseWorldPosition.x, mouseWorldPosition.y + 1, gameData));
                        boolean valid = true;
                        for(GameObjectID id : seeds) {
                            if(id == null || animationManager.isObjectOccupied(id)) {
                                valid = false;
                                break;
                            }
                        }
                        if(valid && seeds.contains(selectedObject.uniqueID)) {
                            GrowAction growAction = new GrowAction(seeds);
                            if(growAction.validate(clientInfo.clientID, world, gameData)) {
                                connection.queueSend(new ActionCommand(growAction, world));
                                growAction.animate(this);
                            }
                        }
                    }
                    currentCommand = UICommand.NONE;
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
        } else {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                currentCommand = UICommand.NONE;
                worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>());
            }
            if(key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                chatbox.enable();
            } else if(mode == Mode.PLAY) {
                if(world.teams.isClientsTurn(clientInfo.clientID)) {
                    if(currentCommand == UICommand.NONE) {
                        if(key == GLFW_KEY_Q && action == GLFW_PRESS) {
                            GameObject obj = world.gameObjects.get(clickBoxManager.selectedID);
                            if(obj != null && obj.speedLeft > 0 && !animationManager.isObjectOccupied(clickBoxManager.selectedID) && gameData.getType(obj.type).canMove() && obj.alive) {
                                currentCommand = UICommand.MOVE;
                                selectGridManager.regenerateSelect(clickBoxManager.selectedID);
                                worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>(selectGridManager.getSelectionGrid().map.values()));
                            }
                        }
                        if(key == GLFW_KEY_W && action == GLFW_PRESS) {
                            GameObject obj = world.gameObjects.get(clickBoxManager.selectedID);
                            if(obj != null && gameData.getType(obj.type).canSpawn() && obj.speedLeft > 0 && !animationManager.isObjectOccupied(clickBoxManager.selectedID) && obj.alive) {
                                currentCommand = UICommand.SPAWN;
                                Set<Vector2i> options = MathUtil.adjacentTiles(MathUtil.addToAll(gameData.getType(obj.type).getRelativeOccupiedTiles(), new Vector2i(obj.x, obj.y)));
                                List<Vector2i> newOptions = new ArrayList<>();
                                for(Vector2i tile : options) {
                                    if(world.occupied(tile.x, tile.y, gameData) == null && world.getTileWeight(gameData, tile.x, tile.y) < Double.POSITIVE_INFINITY) {
                                        newOptions.add(tile);
                                    }
                                }
                                selectGridManager.regenerateSelect(newOptions);
                                worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>(selectGridManager.getSelectionGrid().map.values()));
                            }
                        }
                        if(key == GLFW_KEY_E && action == GLFW_PRESS) {
                            GameObject obj = world.gameObjects.get(clickBoxManager.selectedID);
                            if(obj != null && obj.speedLeft >= 5 && !animationManager.isObjectOccupied(clickBoxManager.selectedID) && obj.alive) {
                                currentCommand = UICommand.ATTACK;
                                Set<Vector2i> options = MathUtil.adjacentTiles(MathUtil.addToAll(gameData.getType(obj.type).getRelativeOccupiedTiles(), new Vector2i(obj.x, obj.y)));
                                List<Vector2i> newOptions = new ArrayList<>();
                                for(Vector2i tile : options) {
                                    GameObjectID victimID = world.occupied(tile.x, tile.y, gameData);
                                    if(victimID != null) {
                                        GameObject victim = world.gameObjects.get(victimID);
                                        if(victim.alive && (victim.team == null || !victim.team.equals(obj.team))) {
                                            newOptions.add(tile);
                                        }
                                    }
                                }
                                selectGridManager.regenerateSelect(newOptions);
                                worldRenderer.tileGridRenderer.buildSelect(new ArrayList<>(selectGridManager.getSelectionGrid().map.values()));
                            }
                        }
                        if(key == GLFW_KEY_R && action == GLFW_PRESS) {
                            GameObject obj = world.gameObjects.get(clickBoxManager.selectedID);
                            if(obj != null && !animationManager.isObjectOccupied(clickBoxManager.selectedID) && obj.alive) {
                                boolean squareExists = false;
                                for(Vector2i[] square : MathUtil.SQUARE_DIRECTIONS_DIAGONAL) {
                                    boolean allPresent = true;
                                    for(Vector2i tileOffset : square) {
                                        Vector2i tile = new Vector2i(tileOffset).add(obj.x, obj.y);
                                        GameObjectID id = world.occupied(tile.x, tile.y, gameData);
                                        if(id == null) {
                                            allPresent = false;
                                            break;
                                        }
                                        GameObject tileObj = world.gameObjects.get(id);
                                        if(!gameData.getType(tileObj.type).canGrow()) {
                                            allPresent = false;
                                            break;
                                        }
                                    }
                                    if(allPresent) {
                                        squareExists = true;
                                    }
                                }
                                if(squareExists) {
                                    currentCommand = UICommand.GROW;
                                }
                            }
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
                        currentCommand = UICommand.NONE;
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
            camera.move();

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
                for(int i = GLFW_KEY_1; i <= GLFW_KEY_9; ++i) {
                    if(glfwGetKey(window, i) == GLFW_PRESS) {
                        ClientID clientID = clientInfo.clientID;
                        TeamID team = world.teams.getClientTeam(clientID);
                        if(team == null) chatbox.println("Need to have team");
                        GameObject obj = null;
                        if(i - GLFW_KEY_1 >= gameData.getTypes().size()) chatbox.println("Missing entity type");
                        else obj = world.gameObjectFactory.createGameObject(gameData.getTypes().get(i - GLFW_KEY_1), team);
                        if(obj != null) {
                            obj.x = mouseWorldPosition.x;
                            obj.y = mouseWorldPosition.y;
                            if(world.add(obj, gameData)) {
                                worldRenderer.resetGameObjectRenderCache();
                                clickBoxManager.resetGameObjectCache();
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
                                    }
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
                                MathUtil.writeFile(String.join(" ", Arrays.copyOfRange(args, 2, args.length)), gameData.toJSON().toString(4));
                            } else if(args[1].equalsIgnoreCase("load")) {
                                if(args.length < 3) {
                                    chatbox.println("Need file name to load");
                                } else {
                                    String path = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                                    String data = MathUtil.readFile(path);
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
                        } else if(args[0].equals("moveall")) {
                            TeamID team = world.teams.getClientTeam(clientInfo.clientID);
                            if(team != null) {
                                for(GameObject gameObject : world.gameObjects.values()) {
                                    if(gameObject.team.equals(team)) {
                                        Set<Vector2i> targets = Pathfinding.pathPossibilities(SelectGridManager.getWeightStorage(gameObject.uniqueID, world, gameData), new Vector2i(gameObject.x, gameObject.y), gameObject.speedLeft).possiblities();
                                        int n = new Random().nextInt(targets.size());
                                        Vector2i target = new Vector2i();
                                        for(Vector2i v : targets) {
                                            if(n-- == 0) {
                                                target = v;
                                                break;
                                            }
                                        }
                                        MoveAction action = new MoveAction(gameObject.uniqueID, target.x, target.y);
                                        if(action.validate(clientInfo.clientID, world, gameData)) {
                                            action.animate(this);
                                            connection.queueSend(new ActionCommand(action, world));
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
                                MathUtil.writeFile(fileName, obj.toString(4));
                            }
                        } else if(args[0].equals("load")) {
                            if(args.length < 2) {
                                chatbox.println("Requires file name");
                            } else {
                                String fileName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                                JSONObject obj = new JSONObject(MathUtil.readFile(fileName));
                                if(connection.isConnected()) {
                                    connection.queueSend(new SetWorldJSON(obj.toString()));
                                } else {
                                    world.initFromJSON(obj, gameData);
                                    chatbox.println("Successfully reinitialized world");
                                }
                            }
                        } else if(args[0].equals("teamname")) {
                            if(connection.isConnected() && args.length == 2) {
                                connection.queueSend(new SetTeamName(args[1]));
                            } else {
                                chatbox.println("Invalid command/arguments");
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
                Set<Vector2i> occupied = MathUtil.addToAll(gameData.getType(selectedObject.type).getRelativeOccupiedTiles(), new Vector2i(selectedObject.x, selectedObject.y));

                if(currentCommand == UICommand.ATTACK) {
                    GameObject targetObject = clickBoxManager.getGameObjectAtViewPositionExcludeTeam(mouseViewPosition, world.teams.getClientTeam(clientInfo.clientID));
                    if(targetObject != null && !targetObject.team.equals(world.teams.getClientTeam(clientInfo.clientID))) {
                        occupied.addAll(MathUtil.addToAll(gameData.getType(targetObject.type).getRelativeOccupiedTiles(), new Vector2i(targetObject.x, targetObject.y)));
                    }
                } else if(currentCommand == UICommand.GROW) {
                    occupied.add(new Vector2i(mouseWorldPosition.x, mouseWorldPosition.y));
                    occupied.add(new Vector2i(mouseWorldPosition.x + 1, mouseWorldPosition.y));
                    occupied.add(new Vector2i(mouseWorldPosition.x + 1, mouseWorldPosition.y + 1));
                    occupied.add(new Vector2i(mouseWorldPosition.x, mouseWorldPosition.y + 1));
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