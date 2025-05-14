package fr.polytech.pie.model;

import java.util.Observable;
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

    /**
     * Constructor for the model with 2D mode as the default
     */
    public Model(ScheduledExecutorService scheduler) {
        this(scheduler, false);
    }

    /**
     * Constructor that can create a model in either 2D or 3D mode.
     *
     * @param scheduler The scheduler to use for scheduling tasks
     * @param is3D      Whether the game should be in 3D mode
     */
    public Model(ScheduledExecutorService scheduler, boolean is3D) {
        this.game = new Game(is3D);

        // Automatic downward movement
        scheduler.scheduleAtFixedRate(() -> {
            if (is3D) {
                translateCurrentPiece3D(0, 1, 0);
            } else {
                translateCurrentPiece2D(0, 1);
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
    }

    /**
     * Translate the current piece in 2D mode.
     */
    public void translateCurrentPiece2D(int dx, int dy) {
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

    public void runAi() {
        game.runAi();
        setChanged();
        notifyObservers();
    }

    /**
     * Rotate the current piece in 2D mode.
     */
    public void rotateCurrentPiece2D() {
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
