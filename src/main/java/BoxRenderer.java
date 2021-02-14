import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL20.*;

public class BoxRenderer {

    private int simpleShader;
    private int simpleMatrix;
    private int simpleColor;

//    private int squareVao;
    private int squareVbo;

    private static final float[] squareCoords = {
        -0.5f, -0.5f,
        -0.5f, 0.5f,
        0.5f, 0.5f,
        0.5f, 0.5f,
        0.5f, -0.5f,
        -0.5f, -0.5f,
    };

    public BoxRenderer() {
        int vertex = Shaders.createShader("simplev.glsl", GL_VERTEX_SHADER);
        int fragment = Shaders.createShader("simplef.glsl", GL_FRAGMENT_SHADER);
        simpleShader = glCreateProgram();
        glAttachShader(simpleShader, vertex);
        glAttachShader(simpleShader, fragment);
        glBindAttribLocation(simpleShader, Shaders.Attribute.POSITION.position, "position");
        glLinkProgram(simpleShader);
        Shaders.checkLinking(simpleShader);
        glUseProgram(simpleShader);
        simpleMatrix = glGetUniformLocation(simpleShader, "matrix");
        simpleColor = glGetUniformLocation(simpleShader, "color");
        glDeleteShader(vertex);
        glDeleteShader(fragment);
        Shaders.checkGLError("Shader link simple " + simpleShader);

        try(MemoryStack stack = MemoryStack.stackPush()) {
//            squareVao = glGenVertexArrays();
//            glBindVertexArray(squareVao);
            squareVbo = glGenBuffers();
            FloatBuffer squareVerts = stack.mallocFloat(squareCoords.length);
            squareVerts.put(squareCoords);
            squareVerts.flip();
            glBindBuffer(GL_ARRAY_BUFFER, squareVbo);
            glBufferData(GL_ARRAY_BUFFER, squareVerts, GL_STATIC_DRAW);
//            glEnableVertexAttribArray(Shaders.Attribute.POSITION.position);
//            glVertexAttribPointer(Shaders.Attribute.POSITION.position,
//                2, GL_FLOAT, false, 4 * 2, 0);
            Shaders.checkGLError("VBO simple " + simpleShader);
        }
    }

    public void draw(Matrix4f matrix, Vector4f color) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            glUseProgram(simpleShader);
//            glBindVertexArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, squareVbo);
            glEnableVertexAttribArray(Shaders.Attribute.POSITION.position);
            glVertexAttribPointer(Shaders.Attribute.POSITION.position,
                2, GL_FLOAT, false, 4 * 2, 0);
            glUniform4f(simpleColor, color.x, color.y, color.z, color.w);
            glUniformMatrix4fv(simpleMatrix, false, matrix.get(buffer));
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glDisableVertexAttribArray(Shaders.Attribute.POSITION.position);
        }
    }

    public void cleanUp() {
        glDeleteProgram(simpleShader);
//        glDeleteVertexArrays(squareVao);
        glDeleteBuffers(squareVbo);
    }
}
