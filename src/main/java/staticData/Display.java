package staticData;

import game.GameObjectID;
import game.World;
import render.RenderComponent;
import render.WorldRenderer;

public interface Display extends TypeComponent {

    RenderComponent makeRenderComponent(GameObjectID gameObjectID, World world, GameData gameData, WorldRenderer.GameObjectTextures textureLibrary);
}
