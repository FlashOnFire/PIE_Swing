package fr.polytech.pie.vc;

import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.Grid;
import fr.polytech.pie.model.Model;
import fr.polytech.pie.model.threeD.CurrentPiece3D;
import fr.polytech.pie.model.threeD.Grid3D;
import fr.polytech.pie.vc.render.OpenGLRenderer;
import fr.polytech.pie.vc.render.cameras.CameraController;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Renderer3D implements Renderer {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private long window;
    private final OpenGLRenderer renderer = new OpenGLRenderer();
    private CameraController camController;

    private final boolean[] keys = new boolean[GLFW_KEY_LAST];

    private final Model model;
    private double lastTime;

    public Renderer3D(Model m) {
        this.model = m;
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
        glfwWindowHint(GLFW_SAMPLES, 16);

        window = glfwCreateWindow(800, 800, "Tetris", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(
                window, (window, key, _ /*scancode*/, action, _ /*mods*/) -> {
                    if (action == GLFW_PRESS || action == GLFW_RELEASE) {
                        keys[key] = action == GLFW_PRESS;
                    }
                }
        );

        glfwSetScrollCallback(
                window, (_ /*window*/, _ /*w*/, y) -> {
                    camController.handleMouseWheel(1.0F, y);
                }
        );

        glfwSetCursorPosCallback(
                window, (_, xpos, ypos) -> camController.handleMouseInput(0, xpos, ypos)
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

        glEnable(GL_MULTISAMPLE);

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            int width = pWidth.get();
            int height = pHeight.get();

            camController = new CameraController(true, ((float) width) / ((float) height));
        }

        float gridWidth = model.getGame().getGrid().getWidth();
        float gridHeight = model.getGame().getGrid().getHeight();
        float gridDepth = ((Grid3D) model.getGame().getGrid()).getDepth();

        Vector3f center = new Vector3f(gridWidth / 2.0F, gridHeight / 2.0F, gridDepth / 2.0F);

        camController.setDirectedCamTarget(center);

        lastTime = glfwGetTime();
    }

    public LoopStatus loop() {
        if (glfwWindowShouldClose(window)) {
            return LoopStatus.SHOW_MENU;
        }

        double currentTime = glfwGetTime();
        float deltaTime = (float) (currentTime - lastTime);
        lastTime = currentTime;

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            int width = pWidth.get();
            int height = pHeight.get();

            pWidth.clear();
            pHeight.clear();
            glfwGetWindowSize(window, pWidth, pHeight);
            glViewport(0, 0, width, height);
            width = pWidth.get();
            height = pHeight.get();
            camController.setAspectRatio(((float) width) / ((float) height));
        } // the stack frame is popped automatically as MemoryStack implements AutoCloseable

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (keys[GLFW_KEY_ESCAPE]) {
            glfwSetWindowShouldClose(window, true);
        }

        camController.handleKeyboardInput(deltaTime, keys);

        renderer.render(camController.getCurrentProjectionMatrix(), camController.getCurrentViewMatrix());

        glfwSwapBuffers(window);

        glfwPollEvents();

        return LoopStatus.CONTINUE;
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

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwWaitEventsTimeout(1000);
    }
}
