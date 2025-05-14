package fr.polytech.pie.vc;

import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.Grid;
import fr.polytech.pie.model.Model;
import fr.polytech.pie.vc.render.Camera;
import fr.polytech.pie.vc.render.OpenGLRenderer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Objects;
import java.util.Observable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

@SuppressWarnings("deprecation")
public class Renderer3D implements Renderer {

    private final long window;
    private final OpenGLRenderer renderer = new OpenGLRenderer();
    private Camera cam;

    private final boolean[] keys = new boolean[7];

    // Mouse handling variables
    private double lastX = 150.0;
    private double lastY = 150.0;
    private boolean firstMouse = true;
    private final float mouseSensitivity = 0.1f;

    public Renderer3D(ScheduledExecutorService scheduler, Model m) {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        // Request OpenGL 3.3 core profile
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        // For macOS compatibility
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        glfwWindowHint(GLFW_DEPTH_BITS, 24);

        window = glfwCreateWindow(800, 800, "Tetris", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(
                window, (window, key, scancode, action, mods) -> {
                    if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                        glfwSetWindowShouldClose(window, true);
                    } else if (action == GLFW_PRESS || action == GLFW_RELEASE) {
                        boolean pressed = action == GLFW_PRESS;
                        switch (key) {
                            case GLFW_KEY_W -> keys[0] = pressed;
                            case GLFW_KEY_S -> keys[1] = pressed;
                            case GLFW_KEY_A -> keys[2] = pressed;
                            case GLFW_KEY_D -> keys[3] = pressed;
                            case GLFW_KEY_SPACE -> keys[4] = pressed;
                            case GLFW_KEY_LEFT_SHIFT -> keys[5] = pressed;
                        }
                    }
                }
        );


        glfwSetCursorPosCallback(
                window, (window, xpos, ypos) -> {
                    if (firstMouse) {
                        lastX = xpos;
                        lastY = ypos;
                        firstMouse = false;
                    }

                    double xOffset = xpos - lastX;
                    double yOffset = lastY - ypos; // Reversed: y ranges bottom to top in glfw

                    lastX = xpos;
                    lastY = ypos;

                    xOffset *= mouseSensitivity;
                    yOffset *= mouseSensitivity;

                    cam.addYaw((float) Math.toRadians(xOffset));
                    cam.addPitch((float) Math.toRadians(yOffset));

                    // Limit pitch to avoid flipping
                    float pitchLimit = (float) (Math.PI / 2.0 - 0.01);
                    if (cam.getPitch() > pitchLimit) {
                        cam.setPitch(pitchLimit);
                    }
                    if (cam.getPitch() < -pitchLimit) {
                        cam.setPitch(-pitchLimit);
                    }
                }
        );
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            assert videoMode != null;

            // Don't center the window on Wayland to avoid errors
            String wmType = System.getenv("XDG_SESSION_TYPE");
            if (!"wayland".equals(wmType)) {
                glfwSetWindowPos(
                        window,
                        (videoMode.width() - pWidth.get(0)) / 2,
                        (videoMode.height() - pHeight.get(0)) / 2
                );
            }
        } // the stack frame is popped automatically as MemoryStack implements AutoCloseable

        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        glfwShowWindow(window);

        scheduler.scheduleAtFixedRate(
                () -> {
                    if (keys[0]) {
                        cam.moveForward(0.2F);
                    }
                    if (keys[1]) {
                        cam.moveForward(-0.2F);
                    }
                    if (keys[2]) {
                        cam.moveRight(-0.2F);
                    }
                    if (keys[3]) {
                        cam.moveRight(0.2F);
                    }
                    if (keys[4]) {
                        cam.moveUp(0.2F);
                    }
                    if (keys[5]) {
                        cam.moveUp(-0.20F);
                    }
                }, 0, 20, TimeUnit.MILLISECONDS
        );
    }

    public void shutdown() {
    }

    public void loop() {
        GL.createCapabilities();

        renderer.init();

        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

//        glEnable(GL_POLYGON_OFFSET_FILL);
//        glPolygonOffset(1.0f, 2.0f);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            int width = pWidth.get();
            int height = pHeight.get();

            cam = new Camera(((float) width) / ((float) height));

            while (!glfwWindowShouldClose(window)) {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                pWidth.clear();
                pHeight.clear();
                glfwGetWindowSize(window, pWidth, pHeight);
                glViewport(0, 0, width, height);
                width = pWidth.get();
                height = pHeight.get();
                cam.setAspectRatio(((float) width) / ((float) height));

                renderer.render(cam);

                glfwSwapBuffers(window);

                glfwPollEvents();
            }
        } // the stack frame is popped automatically as MemoryStack implements AutoCloseable
    }

    @Override
    public void initialize() {

    }

    @Override
    public void update(Grid grid, CurrentPiece currentPiece, int score) {

    }

    @Override
    public void cleanup() {
        renderer.destroy();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
}
