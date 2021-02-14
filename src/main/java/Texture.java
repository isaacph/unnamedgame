import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture {

    public static class Settings {
        public int wrap, filter;

        public Settings(int wrap, int filter) {
            this.wrap = wrap;
            this.filter = filter;
        }
    }

    public int texture;

    public Texture(String path, Settings settings) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.ints(0);
            IntBuffer h = stack.ints(0);
            IntBuffer bpp = stack.ints(0);
            ByteBuffer data;
            try {
                InputStream stream = Util.getInputStream(path);
                ReadableByteChannel channel = Channels.newChannel(stream);
                data = MemoryUtil.memAlloc(stream.available());
                channel.read(data);
                channel.close();
                data.flip();
            } catch (Exception e) {
                System.err.println("Error loading texture " + path);
                e.printStackTrace();
                return;
            }
            ByteBuffer bitmap = STBImage.stbi_load_from_memory(data, w, h, bpp, 4);
            texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texture);
//            System.out.println(w.get(0) + ", " + h.get(0) + ": " + bpp.get(0));
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(0), h.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, settings.wrap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, settings.wrap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, settings.filter);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, settings.filter);
            glGenerateMipmap(GL_TEXTURE_2D);
            Shaders.checkGLError("Load image " + path);
        }
    }

    public Texture(String path) {
        this(path, new Settings(GL_CLAMP_TO_EDGE, GL_NEAREST));
    }

    public Texture(int width, int height, float[] floats, Settings settings) {
        texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_FLOAT, floats);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, settings.wrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, settings.wrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, settings.filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, settings.filter);
        glGenerateMipmap(GL_TEXTURE_2D);
        Shaders.checkGLError("Create image " + width + " x " + height);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public void cleanUp() {
        glDeleteTextures(texture);
    }
}
