import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL20.*;

public class TextureRenderer {

    private int shader;
    private int shaderMatrix;
    private int shaderColor;
    private int shaderSampler;

//    private int squareVao;
    private int squareVbo;

    private static final float[] squareCoords = {
        -0.5f, -0.5f, 0.0f, 0.0f,
        -0.5f, 0.5f, 0.0f, 1.0f,
        0.5f, 0.5f, 1.0f, 1.0f,
        0.5f, 0.5f, 1.0f, 1.0f,
        0.5f, -0.5f, 1.0f, 0.0f,
        -0.5f, -0.5f, 0.0f, 0.0f
    };

    public TextureRenderer() {
        int vertex = Shaders.createShader("texturev.glsl", GL_VERTEX_SHADER);
        int fragment = Shaders.createShader("texturef.glsl", GL_FRAGMENT_SHADER);
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
        shaderSampler = glGetUniformLocation(shader, "sampler");
        glDeleteShader(vertex);
        glDeleteShader(fragment);
        Shaders.checkGLError("Shader link simple " + shader);

        try(MemoryStack stack = MemoryStack.stackPush()) {
//            squareVao = glGenVertexArrays();
//            glBindVertexArray(squareVao);
            squareVbo = glGenBuffers();
            FloatBuffer squareVerts = stack.mallocFloat(squareCoords.length);
            squareVerts.put(squareCoords);
            squareVerts.flip();
            glBindBuffer(GL_ARRAY_BUFFER, squareVbo);
            glBufferData(GL_ARRAY_BUFFER, squareVerts, GL_STATIC_DRAW);
            glEnableVertexAttribArray(Shaders.Attribute.POSITION.position);
            glVertexAttribPointer(Shaders.Attribute.POSITION.position,
                2, GL_FLOAT, false, 4 * 4, 0);
            glEnableVertexAttribArray(Shaders.Attribute.TEXTURE.position);
            glVertexAttribPointer(Shaders.Attribute.TEXTURE.position,
                2, GL_FLOAT, false, 4 * 4, 4 * 2);
            Shaders.checkGLError("VBO texture " + shader);
        }
    }

    public void draw(Matrix4f matrix, Vector4f color) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            glBindBuffer(GL_ARRAY_BUFFER, squareVbo);
            glEnableVertexAttribArray(Shaders.Attribute.POSITION.position);
            glVertexAttribPointer(Shaders.Attribute.POSITION.position,
                2, GL_FLOAT, false, 4 * 4, 0);
            glEnableVertexAttribArray(Shaders.Attribute.TEXTURE.position);
            glVertexAttribPointer(Shaders.Attribute.TEXTURE.position,
                2, GL_FLOAT, false, 4 * 4, 4 * 2);
            FloatBuffer buffer = stack.mallocFloat(16);
            glUseProgram(shader);
//            glBindVertexArray(squareVao);
            glUniform4f(shaderColor, color.x, color.y, color.z, color.w);
            glUniformMatrix4fv(shaderMatrix, false, matrix.get(buffer));
            glUniform1i(shaderSampler, 0);
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glDisableVertexAttribArray(Shaders.Attribute.POSITION.position);
            glDisableVertexAttribArray(Shaders.Attribute.TEXTURE.position);
        }
    }

    public void cleanUp() {
        glDeleteProgram(shader);
//        glDeleteVertexArrays(squareVao);
        glDeleteBuffers(squareVbo);
    }
}
