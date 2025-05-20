package fr.polytech.pie.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private int highScore2D = 0;
    private int highScore3D = 0;

    /**
     * Constructor for the model with 2D mode as the default
     */
    public Model() {
        this(false);
        loadHighScore();
    }

    /**
     * Constructor that can create a model in either 2D or 3D mode.
     *
     * @param is3D Whether the game should be in 3D mode
     */
    public Model(boolean is3D) {
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
        if (game.isGameOver()) {
            stopScheduler();
        }
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

    public void rotateCurrentPiece3D(RotationAxis axis) {
        rotateCurrentPiece3D(axis, false);
    }

    /**
     * Rotate the current piece in 3D mode around the specified axis.
     */
    public void rotateCurrentPiece3D(RotationAxis axis, boolean reverse) {
        if (is3D) {
            game.rotateCurrentPiece3D(axis, reverse);
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

    public void stopGame() {
        stopScheduler();
        System.out.println("Game stopped");
        if (game.getScore() > (is3D ? highScore3D : highScore2D)) {
            setHighScore(game.getScore());
        }
        game.resetGame();
    }

    public void stopScheduler() {
        if (executor != null && !executor.isShutdown()) {
            this.executor.shutdown();
        }
        game.getAi().shutdown();
    }

    public void startScheduler() {
        if (executor == null || executor.isShutdown()) {
            this.executor = Executors.newScheduledThreadPool(1);
        }

        executor.scheduleAtFixedRate(
                () -> {
                    if (this.is3D) {
                        translateCurrentPiece3D(0, -1, 0);
                    } else {
                        translateCurrentPiece2D(0, -1);
                    }
                }, 0, 200 / game.getDifficulty(), TimeUnit.MILLISECONDS
        );
    }

    public int getDroppedYCurrentPiece() {
        CurrentPiece currentPiece = game.getCurrentPiece();
        int originalY = currentPiece.getY();

        CurrentPiece tempPiece = currentPiece.clone();
        int droppedY = originalY;

        do {
            droppedY--;
            tempPiece.setY(droppedY);
        } while (!game.getGrid().checkCollision(tempPiece));

        return droppedY + 1;
    }

    public void dropCurrentPiece() {
        game.getCurrentPiece().setY(getDroppedYCurrentPiece());
    }

    public boolean isGameOver() {
        return game.isGameOver();
    }

    private void loadHighScore() {
        try {
            Path path = Paths.get("high-score.txt");
            if (Files.exists(path)) {
                String content = Files.readString(path).trim();
                String[] scores = content.split(",");
                if (scores.length == 2) {
                    this.highScore2D = Integer.parseInt(scores[0]);
                    this.highScore3D = Integer.parseInt(scores[1]);
                } else {
                    saveHighScore();
                }
            } else {
                saveHighScore();
            }
        } catch (Exception e) {
            System.err.println("Failed to load high-score: " + e.getMessage());
        }
    }

    public void saveHighScore() {
        try {
            Path path = Paths.get("high-score.txt");
            String content = highScore2D + "," + highScore3D;
            Files.writeString(path, content);
        } catch (Exception e) {
            System.err.println("Failed to save high-score: " + e.getMessage());
        }
    }

    public int getHighScore(boolean is3D) {
        return is3D ? highScore3D : highScore2D;
    }

    public void setHighScore(int score) {
        if (is3D) {
            this.highScore3D = score;
        } else {
            this.highScore2D = score;
        }
        saveHighScore();
    }
}
