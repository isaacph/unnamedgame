package render;

import game.Camera;
import game.Grid;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL20.*;

public class TileGridRenderer {

    private int shader;
    private int shaderMatrix;
    private int shaderColor;
    private int shaderSampler1;
    private int shaderSampler2;
    private int shaderVertScale;
    private int shaderNumTiles;
    private int shaderTexMorph;
    private int shaderSamplerTile;
    private int shaderLineWidth;
    private int shaderTextureOffset;
    private int shaderTextureScale;

    /**
     * Every tile's visual height / width
     */
    public static final float TILE_RATIO = 0.6f;
    public static final float TILE_WIDTH = 1.0f;

//    private static final float[] squareCoords = {
//        -0.5f, -0.5f, 0.0f, 0.0f,
//        -0.5f, 0.5f, 0.0f, 1.0f,
//        0.5f, 0.5f, 1.0f, 1.0f,
//        0.5f, 0.5f, 1.0f, 1.0f,
//        0.5f, -0.5f, 1.0f, 0.0f,
//        -0.5f, -0.5f, 0.0f, 0.0f
//    };

    private static class GridInfo {
        public int texture;

        public void cleanUp() {
            glDeleteTextures(texture);
        }
    }

    private Texture grass, grass2;
    private Map<Vector2i, GridInfo> gridMap = new HashMap<>();
    private static final Matrix4f TEX_MORPH =
        new Matrix4f().scale(-(float) Math.sqrt(2), TILE_RATIO * (float) Math.sqrt(2), 0)
            .rotate(45.0f * (float) Math.PI / 180.0f, 0, 0, 1);
    private int vbo;

    private float scale = 1.0f;

    public TileGridRenderer() {
        int vertex = Shaders.createShader("texturev.glsl", GL_VERTEX_SHADER);
        int fragment = Shaders.createShader("gridf.glsl", GL_FRAGMENT_SHADER);
        shader = glCreateProgram();
        glAttachShader(shader, vertex);
        glAttachShader(shader, fragment);
        glBindAttribLocation(shader, Shaders.Attribute.POSITION.position, "position");
        glBindAttribLocation(shader, Shaders.Attribute.TEXTURE.position, "tex");
        glLinkProgram(shader);
        Shaders.checkLinking(shader);
        glUseProgram(shader);
        shaderMatrix = glGetUniformLocation(shader, "matrix");
        shaderColor = glGetUniformLocation(shader, "color");
        shaderSampler1 = glGetUniformLocation(shader, "sampler1");
        shaderSampler2 = glGetUniformLocation(shader, "sampler2");
        shaderSamplerTile = glGetUniformLocation(shader, "samplerTile");
        shaderVertScale = glGetUniformLocation(shader, "vertScale");
        shaderNumTiles = glGetUniformLocation(shader, "numTiles");
        shaderTexMorph = glGetUniformLocation(shader, "texMorph");
        shaderLineWidth = glGetUniformLocation(shader, "lineWidth");
        shaderTextureOffset = glGetUniformLocation(shader, "textureOffset");
        shaderTextureScale = glGetUniformLocation(shader, "textureScale");
        glDeleteShader(vertex);
        glDeleteShader(fragment);
        Shaders.checkGLError("Shader link tile grid " + shader);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4 * 6);
            Vector2f c = new Vector2f(0, 0);
            Vector2f s = new Vector2f(Grid.SIZE / (float) Math.sqrt(2), TILE_RATIO * Grid.SIZE / (float) Math.sqrt(2));
            buffer.put(new float[] {
                c.x, c.y - s.y, -0.5f, -0.5f,
                c.x - s.x, c.y, +0.5f, -0.5f,
                c.x, c.y + s.y, +0.5f, +0.5f,
                c.x, c.y + s.y, +0.5f, +0.5f,
                c.x + s.x, c.y, -0.5f, +0.5f,
                c.x, c.y - s.y, -0.5f, -0.5f,
            });
            buffer.flip();
            this.vbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
            Shaders.checkGLError("Tile Grid VBO init");
        }

        grass = new Texture("grass.png", new Texture.Settings(GL_REPEAT, GL_LINEAR));
        grass2 = new Texture("mock grass 2.png", new Texture.Settings(GL_REPEAT, GL_LINEAR));
        scale = 2.0f;
    }

    public void build(Grid grid) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            GridInfo data = gridMap.get(new Vector2i(grid.x, grid.y));
            ByteBuffer buffer = stack.malloc(Grid.SIZE * Grid.SIZE);
            buffer.put(grid.data);
            buffer.flip();
            if (data == null) {
                data = new GridInfo();
                data.texture = glGenTextures();
            }
            glBindTexture(GL_TEXTURE_2D, data.texture);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, Grid.SIZE, Grid.SIZE, 0, GL_RED, GL_BYTE, buffer);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gridMap.put(new Vector2i(grid.x, grid.y), data);
        }
        Shaders.checkGLError("Tile grid build " + grid.x + ", " + grid.y);
    }

    public void draw(Matrix4f matrix, Vector4f color, float scale) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            glActiveTexture(GL_TEXTURE1);
            grass.bind();
            glActiveTexture(GL_TEXTURE2);
            grass2.bind();
            glUseProgram(shader);
            glUniform4f(shaderColor, color.x, color.y, color.z, color.w);
            glUniform1i(shaderSamplerTile, 0);
            glUniform1i(shaderSampler1, 1);
            glUniform1i(shaderSampler2, 2);
            glUniform1f(shaderVertScale, TILE_RATIO);
            glUniform1i(shaderNumTiles, Grid.SIZE);
            glUniform1f(shaderLineWidth, 1.0f / (Grid.SIZE) / (TILE_WIDTH) / scale * 0.0f);
            glUniform1f(shaderTextureScale, this.scale);

//            Vector2f v2 = Camera.worldToViewSpace(new Vector2f(16.0f, 16.0f).sub(new Vector2f(0.0f, 0.0f))).mul(1.0f / (float) Math.sqrt(2.0f) / Grid.SIZE);
//            System.out.println(v2.x + ", " + v2.y);

            glUniformMatrix4fv(shaderTexMorph, false, TEX_MORPH.get(buffer));
            buffer.clear();
            glActiveTexture(GL_TEXTURE0);
            for (Vector2i key : gridMap.keySet()) {
                GridInfo info = gridMap.get(key);
                glBindTexture(GL_TEXTURE_2D, info.texture);
                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glEnableVertexAttribArray(Shaders.Attribute.POSITION.position);
                glVertexAttribPointer(Shaders.Attribute.POSITION.position,
                    2, GL_FLOAT, false, 4 * 4, 0);
                glEnableVertexAttribArray(Shaders.Attribute.TEXTURE.position);
                glVertexAttribPointer(Shaders.Attribute.TEXTURE.position,
                    2, GL_FLOAT, false, 4 * 4, 4 * 2);
                Vector2f v = Camera.worldToViewSpace(new Vector2f((key.x + 0.5f) * Grid.SIZE,
                    (key.y + 0.5f) * Grid.SIZE));
                glUniformMatrix4fv(shaderMatrix, false, new Matrix4f(matrix)
                    .translate(v.x, v.y, 0).get(buffer));
                Vector2f textureOffset = Camera.worldToViewSpace(new Vector2f(key).mul(Grid.SIZE).sub(new Vector2f(0.0f, 0.0f))).mul(1.0f / (float) Math.sqrt(2) / Grid.SIZE);
//                System.out.println(textureOffset.x + ", " + textureOffset.y);
                textureOffset = new Vector2f((key.x + key.y), (key.x + key.y) * TILE_RATIO);
//                Vector2f textureOffset = new Vector2f();
                glUniform2f(shaderTextureOffset, textureOffset.x, textureOffset.y);
                glDrawArrays(GL_TRIANGLES, 0, 6);
                glDisableVertexAttribArray(Shaders.Attribute.POSITION.position);
                glDisableVertexAttribArray(Shaders.Attribute.TEXTURE.position);
                buffer.clear();
            }
        }
    }

    public void cleanUp() {
        glDeleteProgram(shader);
        glDeleteBuffers(vbo);
        for(GridInfo info : gridMap.values()) {
            info.cleanUp();
        }
    }
}
