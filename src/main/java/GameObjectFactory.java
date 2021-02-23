import org.joml.Vector2f;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public class GameObjectFactory {

    public static final Type kid = new Type("kid", "kid.png", new Vector2f(0), new Vector2f(1));

    public static class Type {
        public String name;
        public String texture;
        public Vector2f textureOffset;
        public Vector2f textureScale;

        private Type(String name, String texture, Vector2f offset, Vector2f scale) {
            this.name = name;
            this.texture = texture;
            this.textureOffset = offset;
            this.textureScale = scale;
        }
    }

    private final Map<String, Texture> texturesLoaded = new HashMap<>();

    private Texture getTexture(String path) {
        Texture texture = texturesLoaded.get(path);
        if(texture == null) {
            texture = new Texture(path, new Texture.Settings(GL_CLAMP_TO_EDGE, GL_NEAREST));
            texturesLoaded.put(path, texture);
        }
        return texture;
    }

    public GameObject createGameObject(Type type) {
        GameObject object = new GameObject(type);
        object.setRenderComponent(new TextureComponent(object, getTexture(type.texture), type.textureOffset, type.textureScale));
        return object;
    }
}
