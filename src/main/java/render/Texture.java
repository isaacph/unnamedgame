package render;

import org.joml.Vector2ic;
import util.FileUtil;
import org.joml.Vector2i;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture {

    public static class Settings {
        public int wrap, filter;

        public Settings(int wrap, int filter) {
            this.wrap = wrap;
            this.filter = filter;
        }
    }

    public final int texture;
    public final Vector2ic size;

    public Texture(int texture, Vector2ic size) {
        this.texture = texture;
        this.size = new Vector2i(size);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public void cleanUp() {
        glDeleteTextures(texture);
    }

    public static Texture makeTexture(int width, int height, FloatBuffer floats, Settings settings) {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_FLOAT, floats);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, settings.wrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, settings.wrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, settings.filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, settings.filter);
        glGenerateMipmap(GL_TEXTURE_2D);
        Shaders.checkGLError("Create image " + width + " x " + height);
        return new Texture(texture, new Vector2i(width, height));
    }

    public static Texture makeTexture(int width, int height, ByteBuffer bytes, Settings settings) {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, bytes);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, settings.wrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, settings.wrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, settings.filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, settings.filter);
        glGenerateMipmap(GL_TEXTURE_2D);
        Shaders.checkGLError("Create image " + width + " x " + height);
        return new Texture(texture, new Vector2i(width, height));
    }

    public static ByteBuffer loadFromFile(String path, Vector2i destSize) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.ints(0);
            IntBuffer h = stack.ints(0);
            IntBuffer bpp = stack.ints(0);
            ByteBuffer data;
            try {
                InputStream stream = FileUtil.getInputStream(path);
                ReadableByteChannel channel = Channels.newChannel(stream);
                data = MemoryUtil.memAlloc(stream.available());
                channel.read(data);
                channel.close();
                data.flip();
            } catch(Exception e) {
                System.err.println("Error loading texture " + path);
                e.printStackTrace();
                return null;
            }
            ByteBuffer bitmap = STBImage.stbi_load_from_memory(data, w, h, bpp, 4);
            destSize.x = w.get();
            destSize.y = h.get();
            return bitmap;
        }
    }

    public static Texture makeTexture(String path, Settings settings) {
        Vector2i size = new Vector2i();
        ByteBuffer bitmap = loadFromFile(path, size);
        if(bitmap == null) {
            return null;
        }
        return makeTexture(size.x, size.y, bitmap, settings);
    }

    public static Texture makeTexture(String path) {
        return makeTexture(path, new Settings(GL_CLAMP_TO_EDGE, GL_NEAREST));
    }

    // doesn't work
    public static Texture makeTextureOutline(String path, Settings settings) {
        Vector2i size = new Vector2i();
        ByteBuffer bitmap = loadFromFile(path, size);
        if(bitmap == null) return null;
        byte[] byteArray = new byte[size.x * size.y * 4];
        byte[] newByteArray = new byte[size.x * size.y * 4];
        bitmap.get(byteArray);
        bitmap.flip();
        for(int x = 0; x < size.x; ++x) {
            for(int y = 0; y < size.y; ++y) {
                boolean outline = Byte.toUnsignedInt(byteArray[(y * size.x + x) * 4 + 3]) > 10;
                byte newValue = outline ? (byte) (int) 255 : (byte) 0;
                if(outline) {
                    for(int dx = Math.max(0, x - 1); dx <= Math.min(size.x - 1, x + 1); ++dx) {
                        for(int dy = Math.max(0, y - 1); dy <= Math.min(size.y - 1, y + 1); ++dy) {
                            for(int c = 0; c < 4; ++c) {
                                newByteArray[(dy * size.x + dx) * 4 + c] = newValue;
                            }
                        }
                    }
                } else {
                    for(int c = 0; c < 4; ++c) {
                        newByteArray[(y * size.x + x) * 4 + c] = newValue;
                    }
                }
            }
        }
        bitmap.put(newByteArray);
        bitmap.flip();
        return makeTexture(size.x, size.y, bitmap, settings);
    }
}
