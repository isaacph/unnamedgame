import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static org.lwjgl.opengl.GL20.*;

public class Font {

    private String source;
    private int size;
    private int shader;
    private int shaderMatrix;
    private int shaderSampler;
    private int shaderColor;

    private static final int CHAR_COUNT = 128;
    private float advance[] = new float[CHAR_COUNT];
    private float lsb[] = new float[CHAR_COUNT];
    private float aHeight, AHeight;
    private int texture;
//    private int vao;
    private int vbo;

    public Font(String source, int size, int bmpWidth, int bmpHeight) {
        this.source = source;
        this.size = size;

        int vertex = Shaders.createShader("textv.glsl", GL_VERTEX_SHADER);
        int fragment = Shaders.createShader("textf.glsl", GL_FRAGMENT_SHADER);
        shader = glCreateProgram();
        glAttachShader(shader, vertex);
        glAttachShader(shader, fragment);
        glBindAttribLocation(shader, Shaders.Attribute.POSITION.position, "position");
        glBindAttribLocation(shader, Shaders.Attribute.TEXTURE.position, "tex");
        glLinkProgram(shader);
        Shaders.checkLinking(shader);
        glUseProgram(shader);
        Shaders.checkGLError("Error using font program " + shader + " path: " + source);
        glDeleteShader(vertex);
        glDeleteShader(fragment);

        shaderMatrix = glGetUniformLocation(shader, "matrix");
        shaderSampler = glGetUniformLocation(shader, "sampler");
        shaderColor = glGetUniformLocation(shader, "color");

        try {
            // NOTE: this font rendering code is taken from a previous project of mine
            InputStream stream = Font.class.getResourceAsStream(source);
            ReadableByteChannel channel = Channels.newChannel(stream);
            ByteBuffer buffer = MemoryUtil.memAlloc(stream.available()); // plz be an int!
            channel.read(buffer);
            channel.close();
            buffer.flip();

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                STBTTFontinfo info = STBTTFontinfo.mallocStack(stack);
                STBTruetype.stbtt_InitFont(info, buffer);
                IntBuffer adv = stack.ints(0);
                IntBuffer l = stack.ints(0);

                float scale = STBTruetype.stbtt_ScaleForPixelHeight(info, (float) size);
                for(int i = 0; i < CHAR_COUNT; i++)
                {
                    STBTruetype.stbtt_GetCodepointHMetrics(info, i, adv, l);
                    advance[i] = adv.get(0) * scale;
                    lsb[i] = l.get(0) * scale;
                }
            }

            float s0[] = new float[CHAR_COUNT];
            float s1[] = new float[CHAR_COUNT];
            float t0[] = new float[CHAR_COUNT];
            float t1[] = new float[CHAR_COUNT];
            float x0[] = new float[CHAR_COUNT];
            float x1[] = new float[CHAR_COUNT];
            float y0[] = new float[CHAR_COUNT];
            float y1[] = new float[CHAR_COUNT];

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                ByteBuffer bitmap;
                bitmap = MemoryUtil.memAlloc(bmpWidth*bmpHeight);
                STBTTPackedchar.Buffer charData = STBTTPackedchar.mallocStack(CHAR_COUNT, stack);
                STBTTPackContext context2 = STBTTPackContext.mallocStack(stack);
                STBTruetype.stbtt_PackBegin(context2, bitmap, bmpWidth, bmpHeight, 0, 1);
                STBTruetype.stbtt_PackFontRange(context2, buffer, 0, (float) size, 0, charData);
                STBTruetype.stbtt_PackEnd(context2);
                STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);

                for(int codePoint = 0; codePoint < CHAR_COUNT; codePoint++)
                {
                    FloatBuffer xpos = stack.floats(0);
                    FloatBuffer ypos = stack.floats(0);
                    STBTruetype.stbtt_GetPackedQuad(charData, bmpWidth, bmpHeight, codePoint, xpos, ypos, q, false);
                    s0[codePoint] = q.s0();
                    s1[codePoint] = q.s1();
                    t0[codePoint] = q.t0();
                    t1[codePoint] = q.t1();
                    x0[codePoint] = q.x0();
                    x1[codePoint] = q.x1();
                    y0[codePoint] = q.y0();
                    y1[codePoint] = q.y1();
                    int i = codePoint;
                    if(i != 32 && (s1[i] - s0[i] == 0 || t1[i] - t0[i] == 0))
                    {
                        throw new InstantiationException("Font texture too small at char " + (char) i + "(" + i + "): " + size + " " + bmpWidth + ", " + bmpHeight);
                    }
                }

                this.aHeight = (t1['a'] - t0['a']) * bmpHeight;
                this.AHeight = (t1['A'] - t0['A']) * bmpHeight;

                int tex = glGenTextures();
                glEnable(GL_TEXTURE_2D);
                glBindTexture(GL_TEXTURE_2D, tex);
                glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RED,
                    bmpWidth,
                    bmpHeight,
                    0,
                    GL_RED,
                    GL_UNSIGNED_BYTE,
                    bitmap
                );
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                Shaders.checkGLError("Texture " + source);
                this.texture = tex;
            }

            try(MemoryStack stack = MemoryStack.stackPush())
            {
                FloatBuffer vertices = stack.mallocFloat(CHAR_COUNT * 6 * 4);
                for(int i = 0; i < CHAR_COUNT; i++)
                {
                    float xl = x0[i], xh = x1[i];
                    float yl = y0[i], yh = y1[i];
                    float sl = s0[i], sh = s1[i];
                    float tl = t0[i], th = t1[i];
                    vertices.put(xl).put(yl).put(sl).put(tl);
                    vertices.put(xl).put(yh).put(sl).put(th);
                    vertices.put(xh).put(yh).put(sh).put(th);
                    vertices.put(xh).put(yh).put(sh).put(th);
                    vertices.put(xh).put(yl).put(sh).put(tl);
                    vertices.put(xl).put(yl).put(sl).put(tl);
                }
                vertices.flip();

//                float[] vert = {
//                    0.0f, 0.0f, 0.0f, 0.0f,
//                    0.0f, 1.0f, 0.0f, 1.0f,
//                    1.0f, 1.0f, 1.0f, 1.0f,
//                    1.0f, 1.0f, 1.0f, 1.0f,
//                    1.0f, 0.0f, 1.0f, 0.0f,
//                    0.0f, 0.0f, 0.0f, 0.0f
//                };
//                FloatBuffer vertices = stack.mallocFloat(vert.length);
//                vertices.put(vert);
//                vertices.flip();

//                vao = glGenVertexArrays();
//                glBindVertexArray(vao);

                vbo = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
                glEnableVertexAttribArray(Shaders.Attribute.POSITION.position);
                glVertexAttribPointer(Shaders.Attribute.POSITION.position,
                    2, GL_FLOAT, false, 4 * 4, 0);
                glEnableVertexAttribArray(Shaders.Attribute.TEXTURE.position);
                glVertexAttribPointer(Shaders.Attribute.TEXTURE.position,
                    2, GL_FLOAT, false, 4 * 4, 2 * 4);
            }
        }
        catch(Exception e) {
            System.err.println("Error loading font " + source);
            e.printStackTrace();
        }
        Shaders.checkGLError("Font init " + source);
    }

    public void draw(String text, float x, float y, Matrix4f proj) {
        draw(text, x, y, proj, new Vector4f(1));
    }

    public void draw(String text, float x, float y, Matrix4f proj, Vector4f color) {
        if(text.length() == 0) {
            return;
        }
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glEnableVertexAttribArray(Shaders.Attribute.POSITION.position);
        glVertexAttribPointer(Shaders.Attribute.POSITION.position,
            2, GL_FLOAT, false, 4 * 4, 0);
        glEnableVertexAttribArray(Shaders.Attribute.TEXTURE.position);
        glVertexAttribPointer(Shaders.Attribute.TEXTURE.position,
            2, GL_FLOAT, false, 4 * 4, 2 * 4);
        glUseProgram(shader);
        glUniform1i(shaderSampler, 0);
        glActiveTexture(GL_TEXTURE0);
        glUniform4f(shaderColor, color.x, color.y, color.z, color.w);
        try(MemoryStack stack = MemoryStack.stackPush()) {
            float currentX = x - lsb[text.charAt(0)], currentY = y;
            for(char c : text.toCharArray()) {
                if(c == '\n') {
                    currentX = x;
                    currentY += size;
                } else if(c == ' ') {
                    currentX += advance[' '];
                } else {
                    FloatBuffer buffer = stack.mallocFloat(16);
                    Matrix4f mat = new Matrix4f();
                    mat.translate(currentX, currentY, 0);
//                    mat.scale(size);

                    glUniformMatrix4fv(shaderMatrix, false, new Matrix4f(proj).mul(mat).get(buffer));
                    glBindTexture(GL_TEXTURE_2D, texture);
                    glDrawArrays(GL_TRIANGLES, (int) c*6, 6);

                    currentX += advance[c];
                }
            }
        }
        glDisableVertexAttribArray(Shaders.Attribute.POSITION.position);
        glDisableVertexAttribArray(Shaders.Attribute.TEXTURE.position);
    }

    public float textWidth(String text) {
        if(text.isEmpty()) return 0;
        float width = lsb[text.charAt(0)];
        for(char c : text.toCharArray()) {
            width += advance[c];
        }
        return width;
    }

    public void cleanUp() {
        glDeleteProgram(shader);
//        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
    }
}
