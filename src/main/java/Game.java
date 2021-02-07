import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {

    // The window handle
    private long window;
    private BoxRenderer boxRenderer;
    private TextureRenderer textureRenderer;
    private Texture rocket;
    private Font font;

    private int screenWidth = 800, screenHeight = 600;

    private Matrix4f proj;
    private Matrix4f view;
    private Matrix4f projView;

    public void run() {
        init();
        loop();
        cleanUp();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(screenWidth, screenHeight, "Unnamed Game", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        windowResize(screenWidth, screenHeight);

        // Set the background color
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(window, true);
            }
            if(key == GLFW_KEY_BACKSPACE && action >= GLFW_PRESS) {
                System.out.println("BACKSPACE");
            }
            if(key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                System.out.println("ENTER");
            }
        });
        glfwSetWindowSizeCallback(window, (win, w, h) -> {
            windowResize(w, h);
        });
        glfwSetCharCallback(window, (win, codepoint) -> {
            System.out.print((char) codepoint);
        });

        this.boxRenderer = new BoxRenderer();
        this.textureRenderer = new TextureRenderer();
        this.rocket = new Texture("rocket1.png");
        this.font = new Font("font.ttf", 48, 512, 512);
    }

    public void windowResize(int width, int height) {
        glViewport(0, 0, width, height);
        proj = new Matrix4f().ortho(0, width, height, 0, 0.0f, 1.0f);
        view = new Matrix4f();
        projView = new Matrix4f(proj).mul(view);
        this.screenWidth = width;
        this.screenHeight = height;
    }

    private void loop() {

        double currentTime = glfwGetTime();
        double lastTime = currentTime;

        /**
         * The number of seconds since the last frame
         */
        double delta;

        double timer = 0;

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            currentTime = glfwGetTime();
            delta = currentTime - lastTime; // almost always 0.01666666666666666
            lastTime = currentTime;

            glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer

            // everything drawing goes here

            rocket.bind();
//            textureRenderer.draw(new Matrix4f(projView).translate(300, 200, 0).scale(90), new Vector4f(1));
            timer += delta;
            textureRenderer.draw(new Matrix4f(projView).translate(200, 200, 0).scale(100), new Vector4f(1));

            boxRenderer.draw(new Matrix4f(projView).translate(650, 50, 0).scale(400, 200, 0), new Vector4f(0, 0, 0, 0.5f));
            font.draw("asjdfoiajsdiofajsoi", 500, 100, new Matrix4f(proj), new Vector4f(1));

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }

        Shaders.checkGLError("End main loop");
    }

    public void cleanUp() {
        boxRenderer.cleanUp();
        textureRenderer.cleanUp();
    }

    public static void main(String[] args) {
        new Game().run();
    }

}