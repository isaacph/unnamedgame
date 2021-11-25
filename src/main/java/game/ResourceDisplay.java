package game;

import model.GameData;
import model.GameObjectID;
import model.TypeComponent;
import model.World;
import org.joml.Vector2fc;
import render.RenderComponent;
import render.Texture;
import render.WorldRenderer;

// extends typecomponent but not really related to game object types or their components lmao TODO
public interface ResourceDisplay extends TypeComponent {

    String getTexture();
    float getSizeMultiplier();
    Vector2fc getOffset();
}
