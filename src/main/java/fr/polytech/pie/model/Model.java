package fr.polytech.pie.model;

import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Model class that handles the game state and user input.
 * Supports both 2D and 3D modes.
 */
@SuppressWarnings("deprecation")
public class Model extends Observable {
    private final Game game;
    private boolean is3D;
    // Extended to support 3D controls (forward/backward movement on z-axis and
    // rotation around x/y/z)
    public boolean[] keys = new boolean[10];

    /**
     * Constructor for the model with default 2D mode.
     */
    public Model() {
        this(false);
    }

    /**
     * Constructor that can create a model in either 2D or 3D mode.
     * 
     * @param is3D Whether the game should be in 3D mode
     */
    public Model(boolean is3D) {
        this.is3D = is3D;
        this.game = new Game(is3D);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (is3D) {
                handle3DControls();
            } else {
                handle2DControls();
            }
        }, 0, 50, TimeUnit.MILLISECONDS);

        // Automatic downward movement
        scheduler.scheduleAtFixedRate(() -> {
            if (is3D) {
                translateCurrentPiece3D(0, 1, 0);
            } else {
                translateCurrentPiece(0, 1);
            }
        }, 0, 200, TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));
    }

    /**
     * Handle 2D controls.
     * Keys: 0=Down, 1=Up, 2=Right, 3=Left, 4=Rotate
     */
    private void handle2DControls() {
        if (keys[0]) {
            translateCurrentPiece(0, -1); // Down
        }
        if (keys[1]) {
            translateCurrentPiece(0, 1); // Up
        }
        if (keys[2]) {
            translateCurrentPiece(-1, 0); // Right
        }
        if (keys[3]) {
            translateCurrentPiece(1, 0); // Left
        }
        if (keys[4]) {
            rotateCurrentPiece(); // Rotate
        }
    }

    /**
     * Handle 3D controls.
     * Keys: 0=Down, 1=Up, 2=Right, 3=Left, 4=RotateZ, 5=Forward, 6=Backward,
     * 7=RotateX, 8=RotateY, 9=Mode Switch
     */
    private void handle3DControls() {
        if (keys[0]) {
            translateCurrentPiece3D(0, -1, 0); // Down
        }
        if (keys[1]) {
            translateCurrentPiece3D(0, 1, 0); // Up
        }
        if (keys[2]) {
            translateCurrentPiece3D(-1, 0, 0); // Right
        }
        if (keys[3]) {
            translateCurrentPiece3D(1, 0, 0); // Left
        }
        if (keys[4]) {
            rotateCurrentPiece3D(RotationAxis.Z); // Rotate around Z
        }
        if (keys[5]) {
            translateCurrentPiece3D(0, 0, 1); // Forward (z+)
        }
        if (keys[6]) {
            translateCurrentPiece3D(0, 0, -1); // Backward (z-)
        }
        if (keys[7]) {
            rotateCurrentPiece3D(RotationAxis.X); // Rotate around X
        }
        if (keys[8]) {
            rotateCurrentPiece3D(RotationAxis.Y); // Rotate around Y
        }
        if (keys[9]) {
            switchRenderingMode(); // Switch between 2D and 3D
        }
    }

    /**
     * Switch between 2D and 3D rendering modes.
     */
    public void switchRenderingMode() {
        is3D = !is3D;
        game.setRenderingMode(is3D);
        setChanged();
        notifyObservers();
    }

    /**
     * Set the rendering mode.
     */
    public void setRenderingMode(boolean is3D) {
        if (this.is3D != is3D) {
            this.is3D = is3D;
            game.setRenderingMode(is3D);
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Check if the game is in 3D mode.
     */
    public boolean is3D() {
        return is3D;
    }

    /**
     * Translate the current piece in 2D mode.
     */
    public void translateCurrentPiece(int dx, int dy) {
        game.translateCurrentPiece2D(dx, dy);
        setChanged();
        notifyObservers();
    }

    /**
     * Translate the current piece in 3D mode.
     */
    public void translateCurrentPiece3D(int dx, int dy, int dz) {
        if (is3D) {
            game.translateCurrentPiece3D(dx, dy, dz);
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Rotate the current piece in 2D mode.
     */
    public void rotateCurrentPiece() {
        game.rotateCurrentPiece();
        setChanged();
        notifyObservers();
    }

    /**
     * Rotate the current piece in 3D mode around the specified axis.
     */
    public void rotateCurrentPiece3D(RotationAxis axis) {
        if (is3D) {
            game.rotateCurrentPiece3D(axis);
            setChanged();
            notifyObservers();
        }
    }

    public Grid getGrid() {
        return game.getGrid();
    }

    /**
     * Set the state of a key.
     */
    public void setKey(int key, boolean state) {
        if (key >= 0 && key < keys.length) {
            keys[key] = state;
        }
    }

    public CurrentPiece getCurrentPiece() {
        return game.getCurrentPiece();
    }

    public int getScore() {
        return game.getScore();
    }

    public void resetGame() {
        game.resetGame();
    }
}
