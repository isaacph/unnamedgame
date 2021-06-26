package game;

import model.GameData;
import model.GameObjectID;
import model.TypeComponent;
import model.World;
import render.RenderComponent;
import render.WorldRenderer;

public interface Display extends TypeComponent {

    RenderComponent makeRenderComponent(GameObjectID gameObjectID, World world, GameData gameData, WorldRenderer.GameObjectTextures textureLibrary);
}
