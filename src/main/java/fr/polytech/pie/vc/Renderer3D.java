package fr.polytech.pie.vc;

import fr.polytech.pie.model.*;
import fr.polytech.pie.model.threeD.CurrentPiece3D;
import fr.polytech.pie.model.threeD.Grid3D;
import fr.polytech.pie.vc.render.Cube;
import fr.polytech.pie.vc.render.OpenGLRenderer;
import fr.polytech.pie.vc.render.cameras.CameraController;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
    private final boolean[] lastKeys = new boolean[GLFW_KEY_LAST];

    private boolean shiftModifier = false;

    private float pieceForwardTimeCounter = 0;
    private float pieceBackwardTimeCounter = 0;
    private float pieceLeftTimeCounter = 0;
    private float pieceRightTimeCounter = 0;
    private float pieceAiTimeCounter = 0;

    private float pieceForwardRotationTimeCounter = 0;
    private float pieceRightRotationTimeCounter = 0;

    private final VueController vueController;
    private double lastLoopTime;
    private int score = 0;

    private boolean isGameOver = false;
    private float elapsedTimeSinceGameOver = 0;

    public Renderer3D(VueController vueController) {
        this.vueController = vueController;
     }

    @Override
    public void initialize() {
        GLFWErrorCallback.createPrint(System.err).set();
        // This is called from the main thread (since the call path is either from VueController constructor or loop() method)
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        // Request OpenGL 3.3 core profile
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        glfwWindowHint(GLFW_DEPTH_BITS, 24);
        glfwWindowHint(GLFW_SAMPLES, 16);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);

        window = glfwCreateWindow(800, 800, "Tetris", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        //noinspection resource (first key callback setup so previous must be null)
        glfwSetKeyCallback(
                window, (_ /*window*/, key, _ /*scancode*/, action, mods) -> {
                    if (action == GLFW_PRESS || action == GLFW_RELEASE) {
                        keys[key] = action == GLFW_PRESS;
                    }

                    shiftModifier = (mods & GLFW_MOD_SHIFT) != 0;
                }
        );

        //noinspection resource (first key callback setup so previous must be null)
        glfwSetScrollCallback(
                window, (_ /*window*/, _ /*w*/, y) -> camController.handleMouseWheel(1.0F, y)
        );

        //noinspection resource (first key callback setup so previous must be null)
        glfwSetCursorPosCallback(
                window, (_, xpos, ypos) -> camController.handleMouseInput(xpos, ypos)
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

            camController = new CameraController(false, ((float) width) / ((float) height));
        }

        float gridWidth = vueController.getModel().getGame().getGrid().getWidth();
        float gridHeight = vueController.getModel().getGame().getGrid().getHeight();
        float gridDepth = ((Grid3D) vueController.getModel().getGame().getGrid()).getDepth();

        Vector3f center = new Vector3f(gridWidth / 2.0F, gridHeight / 2.0F, gridDepth / 2.0F);

        camController.setDirectedCamTarget(center);

        lastLoopTime = glfwGetTime();
    }

    public LoopStatus loop() {
        if (glfwWindowShouldClose(window) || glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            return LoopStatus.SHOW_MENU;
        }

        double currentTime = glfwGetTime();
        float deltaTime = (float) (currentTime - lastLoopTime);
        lastLoopTime = currentTime;

        if (isGameOver) {
            elapsedTimeSinceGameOver += deltaTime;
            if (elapsedTimeSinceGameOver > 5.0F) {
                return LoopStatus.SHOW_MENU;
            }
        }

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

            renderer.render(window, new Vector2i(width, height), camController.getCurrentProjectionMatrix(), camController.getCurrentViewMatrix(), score, isGameOver, elapsedTimeSinceGameOver);
        } // the stack frame is popped automatically as MemoryStack implements AutoCloseable

        camController.handleKeyboardInput(deltaTime, keys, lastKeys);
        handleKeyboardInput(deltaTime, keys);
        System.arraycopy(keys, 0, lastKeys, 0, lastKeys.length);

        glfwPollEvents();

        return LoopStatus.CONTINUE;
    }

    private void handleKeyboardInput(float deltaTime, boolean[] keys) {
        pieceForwardTimeCounter += deltaTime;
        pieceBackwardTimeCounter += deltaTime;
        pieceLeftTimeCounter += deltaTime;
        pieceRightTimeCounter += deltaTime;
        pieceAiTimeCounter += deltaTime;

        pieceForwardRotationTimeCounter += deltaTime;
        pieceRightRotationTimeCounter += deltaTime;

        if (keys[GLFW_KEY_SPACE] && !lastKeys[GLFW_KEY_SPACE]) {
            vueController.getModel().dropCurrentPiece();
        }

        if (keys[GLFW_KEY_R] && !lastKeys[GLFW_KEY_R]) {
            vueController.getModel().resetGame();
        }

        if (keys[GLFW_KEY_LEFT_ALT] && !lastKeys[GLFW_KEY_LEFT_ALT]) {
            if (glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_NORMAL) {
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            } else {
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            }
        }

        if (!camController.isFreeCam()) {
            float horizontalAngle = camController.getDirectedCam().getHorizontalAngle();

            float forwardX = (float) Math.sin(horizontalAngle);
            float forwardZ = (float) Math.cos(horizontalAngle);
            float rightX = (float) Math.sin(horizontalAngle + Math.PI / 2);
            float rightZ = (float) Math.cos(horizontalAngle + Math.PI / 2);

            // Normalize direction vectors
            float forwardLength = (float) Math.sqrt(forwardX * forwardX + forwardZ * forwardZ);
            float rightLength = (float) Math.sqrt(rightX * rightX + rightZ * rightZ);

            if (forwardLength > 0) {
                forwardX /= forwardLength;
                forwardZ /= forwardLength;
            }

            if (rightLength > 0) {
                rightX /= rightLength;
                rightZ /= rightLength;
            }

            RotationAxis forwardRotationAxis = forwardX > forwardZ ? RotationAxis.Z : RotationAxis.X;

            if (keys[GLFW_KEY_LEFT_SHIFT]) {
                if (keys[GLFW_KEY_W] && pieceForwardRotationTimeCounter > 0.1F) {
                    vueController.getModel().rotateCurrentPiece3D(forwardRotationAxis);
                    pieceForwardRotationTimeCounter = 0;
                }
                if (keys[GLFW_KEY_A] && pieceRightRotationTimeCounter > 0.1F) {
                    vueController.getModel().rotateCurrentPiece3D(RotationAxis.Y);
                    pieceRightRotationTimeCounter = 0;
                }
                if (keys[GLFW_KEY_S] && pieceForwardRotationTimeCounter > 0.1F) {
                    vueController.getModel().rotateCurrentPiece3D(forwardRotationAxis, true);
                    pieceForwardRotationTimeCounter = 0;
                }
                if (keys[GLFW_KEY_D] && pieceRightRotationTimeCounter > 0.1F) {
                    vueController.getModel().rotateCurrentPiece3D(RotationAxis.Y, true);
                    pieceRightRotationTimeCounter = 0;
                }
            } else {
                if (keys[GLFW_KEY_W] && pieceForwardTimeCounter > 0.1F) {
                    vueController.getModel().translateCurrentPiece3D(Math.round(-forwardX), 0, Math.round(-forwardZ));
                    pieceForwardTimeCounter = 0;
                }
                if (keys[GLFW_KEY_S] && pieceBackwardTimeCounter > 0.1F) {
                    vueController.getModel().translateCurrentPiece3D(Math.round(forwardX), 0, Math.round(forwardZ));
                    pieceBackwardTimeCounter = 0;
                }
                if (keys[GLFW_KEY_A] && pieceLeftTimeCounter > 0.1F) {
                    vueController.getModel().translateCurrentPiece3D(Math.round(-rightX), 0, Math.round(-rightZ));
                    pieceLeftTimeCounter = 0;
                }
                if (keys[GLFW_KEY_D] && pieceRightTimeCounter > 0.1F) {
                    vueController.getModel().translateCurrentPiece3D(Math.round(rightX), 0, Math.round(rightZ));
                    pieceRightTimeCounter = 0;
                }
                if (keys[GLFW_KEY_I] && pieceAiTimeCounter > (shiftModifier ? 0.01F : 0.1F)) {
                    vueController.getModel().runAi();
                    pieceAiTimeCounter = 0;
                }
            }
        }
    }

    @Override
    public void update(Grid grid, CurrentPiece currentPiece, CurrentPiece nextPiece, int score, boolean isGameOver) {
        assert currentPiece != null : "Current piece is null";
        assert grid != null : "Grid is null";

        assert grid instanceof Grid3D;

        Grid3D grid3D = (Grid3D) grid;

        List<Cube> cubes = new ArrayList<>();

        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                for (int z = 0; z < grid3D.getDepth(); z++) {
                    Piece value = grid3D.getValue(x, y, z);
                    if (value != Piece.Empty) {
                        Cube cube = new Cube(new Vector3f(x, y, z), isGameOver ? new Vector3f(1.0F, 0.0F, 0.F) : value.getVector());
                        cubes.add(cube);
                    }
                }
            }
        }

        CurrentPiece3D currentPiece3D = (CurrentPiece3D) currentPiece;
        Piece[][][] positions = currentPiece3D.getPiece3d();

        Vector3f piecePos = new Vector3f(currentPiece3D.getX(), currentPiece3D.getY(), currentPiece3D.getZ());
        int fallenPieceY = vueController.getModel().getDroppedYCurrentPiece();
        Vector3f fallenPiecePos = new Vector3f(currentPiece3D.getX(), fallenPieceY, currentPiece3D.getZ());

        for (int x = 0; x < positions[0][0].length; x++) {
            for (int y = 0; y < positions[0].length; y++) {
                for (int z = 0; z < positions.length; z++) {
                    Piece value = positions[z][y][x];
                    if (value != Piece.Empty) {
                        cubes.add(new Cube(new Vector3f(x, y, z).add(piecePos), isGameOver ? new Vector3f(1.0F, 0.0F, 0.F) : value.getVector()));

                        if (fallenPieceY != currentPiece3D.getY()) {
                            cubes.add(new Cube(new Vector3f(x, y, z).add(fallenPiecePos), isGameOver ? new Vector3f(1.0F, 0.0F, 0.F) : new Vector3f(0.5F, 0.5F, 0.5F)));
                        }
                    }
                }
            }
        }

        if (!isGameOver) {
            CurrentPiece3D nextPiece3D = (CurrentPiece3D) nextPiece;
            Piece[][][] nextPositions = nextPiece3D.getPiece3d();

            Vector3f nextPos = new Vector3f(grid.getWidth() / 2.0F - nextPiece.getWidth() / 2.0F, grid.getHeight() - nextPiece.getHeight() / 2.0F + 10.0F, grid3D.getDepth() / 2.0F - nextPiece3D.getDepth() / 2.0F);
            Vector3f nextPieceColor = new Vector3f(0.0F, 1.0F, 0.0F);

            for (int x = 0; x < nextPositions[0][0].length; x++) {
                for (int y = 0; y < nextPositions[0].length; y++) {
                    for (int z = 0; z < nextPositions.length; z++) {
                        Piece value = nextPositions[z][y][x];
                        if (value != Piece.Empty) {
                            cubes.add(new Cube(new Vector3f(x, y, z).add(nextPos), nextPieceColor));
                        }
                    }
                }
            }
        }

        renderer.updateCubes(cubes);
        this.score = score;
        this.isGameOver = isGameOver;
    }

    @Override
    public void cleanup() {
        glfwMakeContextCurrent(window);
        glfwPollEvents();

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

        scheduler.shutdownNow();
        renderer.destroy();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwWaitEventsTimeout(1000);

        // Cleanup GLFW
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).close();
    }
}
