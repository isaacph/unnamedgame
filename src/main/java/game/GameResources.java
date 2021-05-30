package game;

import render.AnimationManager;
import render.WorldRenderer;
import server.ServerPayload;
import staticData.GameData;

public class GameResources {

    public final Camera camera;
    public final Chatbox chatbox;
    public final GameObjectFactory gameObjectFactory;

    public final World world;
    public final WorldRenderer worldRenderer;
    public final AnimationManager animationManager;
    public final ClickBoxManager clickBoxManager;
    public final SelectGridManager selectGridManager;

    public final GameData gameData;
    public final GameTime gameTime;

    public final ClientConnection<ClientPayload, ServerPayload> connection;
    public final ClientInfo clientInfo;

    public GameResources(Camera camera,
                         Chatbox chatbox,
                         GameObjectFactory gameObjectFactory,
                         World world,
                         WorldRenderer worldRenderer,
                         AnimationManager animationManager,
                         ClickBoxManager clickBoxManager,
                         SelectGridManager selectGridManager,
                         GameData gameData,
                         GameTime gameTime,
                         ClientConnection<ClientPayload, ServerPayload> connection,
                         ClientInfo clientInfo) {

        this.camera = camera;
        this.chatbox = chatbox;
        this.gameObjectFactory = gameObjectFactory;
        this.world = world;
        this.worldRenderer = worldRenderer;
        this.animationManager = animationManager;
        this.clickBoxManager = clickBoxManager;
        this.gameData = gameData;
        this.gameTime = gameTime;
        this.connection = connection;
        this.clientInfo = clientInfo;
        this.selectGridManager = selectGridManager;
    }
}
