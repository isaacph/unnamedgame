import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class SpriteRenderer {

    public final int program;
    public int[] uniforms;
    private int vao;
    private int vbo;
    private int uniformMatrix, uniformColor, uniformSampler, uniformSpritePos, uniformFrame;

    public SpriteRenderer() {
        float scale = 1;
        float[] triangles = {
            -0.5f, -0.5f, 0.0f, 0.0f,
            -0.5f, +0.5f, 0.0f, scale,
            +0.5f, +0.5f, scale, scale,
            +0.5f, +0.5f, scale, scale,
            +0.5f, -0.5f, scale, 0.0f,
            -0.5f, -0.5f, 0.0f, 0.0f
        };

        int vert = Shaders.createShader("spritev.glsl", GL_VERTEX_SHADER);
        int frag = Shaders.createShader("texturef.glsl", GL_FRAGMENT_SHADER);
        program = glCreateProgram();
        glAttachShader(program, vert);
        glAttachShader(program, frag);
        glBindAttribLocation(program, Shaders.Attribute.POSITION.position, "position");
        glBindAttribLocation(program, Shaders.Attribute.TEXTURE.position, "texture");
        glLinkProgram(program);
        Shaders.checkLinking(program);
        glUseProgram(program);
        glDeleteShader(vert);
        glDeleteShader(frag);
        Shaders.checkGLError("texture shader init");
        uniformMatrix = glGetUniformLocation(program, "matrix");
        uniformColor = glGetUniformLocation(program, "color");
        uniformSampler = glGetUniformLocation(program, "sampler");
        uniformSpritePos = glGetUniformLocation(program, "spritePos");
        uniformFrame = glGetUniformLocation(program, "spriteFrame");

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, triangles, GL_STATIC_DRAW);
        glEnableVertexAttribArray(Shaders.Attribute.POSITION.position);
        glVertexAttribPointer(Shaders.Attribute.POSITION.position, 2, GL_FLOAT, false, 4 * 4, 0);
        glEnableVertexAttribArray(Shaders.Attribute.TEXTURE.position);
        glVertexAttribPointer(Shaders.Attribute.TEXTURE.position, 2, GL_FLOAT, false, 4 * 4, 2 * 4);
        Shaders.checkGLError("texture buffer init");
    }

    public void draw(Matrix4f matrix, Vector4f color, Vector2f spritePos, Vector2f spriteFrame) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            glBindVertexArray(vao);
            glUseProgram(program);
            glUniform4f(uniformColor, color.x, color.y, color.z, color.w);
            glUniformMatrix4fv(uniformMatrix, false, matrix.get(buffer));
            glUniform1i(uniformSampler, 0);
            glUniform2f(uniformSpritePos, spritePos.x, spritePos.y);
            glUniform2f(uniformFrame, spriteFrame.x, spriteFrame.y);
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }
    }

    public void destroy() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        glDeleteProgram(program);
        Shaders.checkGLError("texture cleanup");
    }
}
