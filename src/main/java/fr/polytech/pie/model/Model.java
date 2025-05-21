package fr.polytech.pie.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Observable;

/**
 * Model class that handles the game state and user input.
 * Supports both 2D and 3D modes.
 */
@SuppressWarnings("deprecation")
public class Model extends Observable {
    private Game game;

    private int difficulty = 1;

    private long highScore2D = 0;
    private long highScore3D = 0;

    public Model() {
        loadHighScore();
    }

    public void startGame(boolean is3D) {
        if (game == null) {
            game = new Game(is3D, difficulty, () -> {
                setChanged();
                notifyObservers();
            });
        } else {
            game.resetGame();

            if (game.is3D() != is3D) {
                changeRenderingMode(is3D);
            }
        }

        setChanged();
        notifyObservers();
    }

    public void changeRenderingMode(boolean is3D) {
        game.setRenderingMode(is3D);
        setChanged();
        notifyObservers();
    }

    public void translateCurrentPiece(TetrisVector translation) {
        game.translateCurrentPiece(translation);

        setChanged();
        notifyObservers();
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
        if (game.is3D()) {
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
        if (game.getScore() > (game.is3D() ? highScore3D : highScore2D)) {
            setHighScore(game.getScore(), game.is3D());
        }

        game.cleanup();
        game = null;
    }

    public void cleanup() {
        if (game != null) {
            game.cleanup();
        }
    }

    public int getDroppedYCurrentPiece() {
        Piece piece = game.getCurrentPiece();
        int originalY = piece.getPosition().getY();

        Piece tempPiece = piece.clone();
        int droppedY = originalY;

        do {
            tempPiece.getPosition().setY(--droppedY);
        } while (!game.getGrid().checkCollision(tempPiece));

        return droppedY + 1;
    }

    public void dropCurrentPiece() {
        game.getCurrentPiece().getPosition().setY(getDroppedYCurrentPiece());
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
                    this.highScore2D = Long.parseLong(scores[0]);
                    this.highScore3D = Long.parseLong(scores[1]);
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

    public long getHighScore(boolean is3D) {
        return is3D ? highScore3D : highScore2D;
    }

    public void setHighScore(int score, boolean is3D) {
        if (is3D) {
            this.highScore3D = score;
        } else {
            this.highScore2D = score;
        }
        saveHighScore();
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
}
