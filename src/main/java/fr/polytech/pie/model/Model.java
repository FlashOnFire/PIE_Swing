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
    private ScheduledExecutorService executor;

    /**
     * Constructor for the model with 2D mode as the default
     */
    public Model() {
        this(false);
    }

    /**
     * Constructor that can create a model in either 2D or 3D mode.
     *
     * @param is3D      Whether the game should be in 3D mode
     */
    public Model( boolean is3D) {
        this.is3D = is3D;
        this.game = new Game(is3D);
    }

    public void changeRenderingMode(boolean is3D) {
        this.is3D = is3D;
        game.setRenderingMode(is3D);
        setChanged();
        notifyObservers();
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

    /**
     * Run the AI for the current game state.
     */
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

    public Game getGame() {
        return game;
    }

    public void resetGame() {
        game.resetGame();
    }

    public void stopScheduler() {
        if (executor != null && !executor.isShutdown()) {
            this.executor.shutdown();
        }
    }

    public void startScheduler() {
        if (executor == null || executor.isShutdown()) {
            this.executor = Executors.newScheduledThreadPool(1);
        }

        // Automatic downward movement
        executor.scheduleAtFixedRate(
                () -> {
                    if (this.is3D) {
                        translateCurrentPiece3D(0, -1, 0);
                    } else {
                        translateCurrentPiece2D(0, -1);
                    }
                }, 0, 200, TimeUnit.MILLISECONDS
        );
    }

    public void dropCurrentPiece() {
        do {
            game.getCurrentPiece().setY(game.getCurrentPiece().getY() - 1);
        } while (!game.getGrid().checkCollision(game.getCurrentPiece()));
        game.getCurrentPiece().setY(game.getCurrentPiece().getY() + 1);
    }
}
