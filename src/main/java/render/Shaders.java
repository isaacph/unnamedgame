package render;

import util.MathUtil;
import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;

public final class Shaders {

    public static int createShader(String path, int type) {
        try {
//            InputStream stream = Shaders.class.getResourceAsStream(path);
            InputStream stream = MathUtil.getInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer content = new StringBuffer();
            while(reader.ready()) {
                content.append(reader.readLine()).append("\n");
            }
            int shader = glCreateShader(type);
            glShaderSource(shader, content);
            glCompileShader(shader);
            checkShaderCompilation(shader);
            checkGLError("Shader compile " + path);
            return shader;
        } catch(Exception e) {
            System.err.println(path);
            e.printStackTrace();
        }
        return -1;
    }

    public static void checkShaderCompilation(int shader)
    {
        IntBuffer success = BufferUtils.createIntBuffer(1);
        glGetShaderiv(shader, GL_COMPILE_STATUS, success);
        if(success.get(0) == GL_FALSE)
        {
            IntBuffer logSize = BufferUtils.createIntBuffer(1);
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, logSize);
            String errorLog = glGetShaderInfoLog(shader);
            throw new RuntimeException("Shader compilation error!\n" + errorLog);
        }
    }

    public static void checkLinking(int program) {
        int[] isLinked = {0};
        glGetProgramiv(program, GL_LINK_STATUS, isLinked);
        if (isLinked[0] == GL_FALSE)
        {
            String log = glGetProgramInfoLog(program);
            System.err.println(log);

            // The program is useless now. So delete it.
            glDeleteProgram(program);

            // Provide the infolog in whatever manner you deem best.
            // Exit with failure.
            return;
        }
    }

    public enum Attribute {
        POSITION(0), TEXTURE(1);

        int position;
        Attribute(int position) {
            this.position = position;
        }
    }



    public static void checkGLError(String message) {
        int err = glGetError();
        if(err != 0)
        {
            throw new RuntimeException("OpenGL error: " + err + "\n" + message);
        }
    }
}
