package fr.polytech.pie.vc;

import fr.polytech.pie.model.*;
import fr.polytech.pie.model.threeD.CurrentPiece3D;
import fr.polytech.pie.model.threeD.Grid3D;
import fr.polytech.pie.vc.render.Camera;
import fr.polytech.pie.vc.render.OpenGLRenderer;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Renderer3D implements Renderer {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private long window;
    private final OpenGLRenderer renderer = new OpenGLRenderer();
    private Camera cam;

    private final boolean[] keys = new boolean[7];

    // Mouse handling variables
    private double lastX = 150.0;
    private double lastY = 150.0;
    private boolean firstMouse = true;
    private final float mouseSensitivity = 0.1f;

    public Renderer3D(Model m) {

    }

    public LoopStatus loop() {
        if (glfwWindowShouldClose(window)) {
            return LoopStatus.SHOW_MENU;
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            int width = pWidth.get();
            int height = pHeight.get();


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
        } // the stack frame is popped automatically as MemoryStack implements AutoCloseable

        return LoopStatus.CONTINUE;
    }

    @Override
    public void initialize() {
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
                window, (window, key, _ /*scancode*/, action, _ /*mods*/) -> {
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
                window, (_, xpos, ypos) -> {
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
        }
    }

    @Override
    public void update(Grid grid, CurrentPiece currentPiece, int score) {
        assert currentPiece != null : "Current piece is null";
        assert grid != null : "Grid is null";

        assert grid instanceof Grid3D;

        Grid3D grid3D = (Grid3D) grid;

        List<Vector3f> cubesPos = new ArrayList<>();
        List<Vector3f> colors = new ArrayList<>();


        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                for (int z = 0; z < grid3D.getDepth(); z++) {
                    if (grid3D.getValue(x, y, z)) {
                        Vector3f pos = new Vector3f(x, y, z);
                        Vector3f color = new Vector3f(0.0F, 0.0F, 1.0F);

                        cubesPos.add(pos);
                        colors.add(color);
                    }
                }
            }
        }

        CurrentPiece3D currentPiece3D = (CurrentPiece3D) currentPiece;
        boolean[][][] positions = currentPiece3D.getPiece3d();

        Vector3f piecePos = new Vector3f(currentPiece3D.getX(), currentPiece3D.getY(), currentPiece3D.getZ());

        for (int x = 0; x < positions[0][0].length; x++) {
            for (int y = 0; y < positions[0].length; y++) {
                for (int z = 0; z < positions.length; z++) {
                    if (positions[z][y][x]) {
                        cubesPos.add(new Vector3f(x, y, z).add(piecePos));
                        colors.add(new Vector3f(1.0F, 0.0F, 0.0F));
                    }
                }
            }
        }
        try {
            renderer.update(cubesPos.toArray(new Vector3f[0]), colors.toArray(new Vector3f[0]));
        } catch (Exception e) {
            Logger.getLogger(Renderer3D.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public void cleanup() {
        glfwMakeContextCurrent(window);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

        scheduler.shutdownNow();
        renderer.destroy();

        Objects.requireNonNull(glfwSetKeyCallback(window, null)).free();
        Objects.requireNonNull(glfwSetCursorPosCallback(window, null)).free();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwWaitEventsTimeout(1000);
    }
}
