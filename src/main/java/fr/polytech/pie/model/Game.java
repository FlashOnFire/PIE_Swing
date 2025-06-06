package fr.polytech.pie.model;

import fr.polytech.pie.Consts;
import fr.polytech.pie.model.threeD.Piece3D;
import fr.polytech.pie.model.twoD.Piece2D;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Game {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;

    private final Runnable updateModelLambda;

    private Grid grid;
    private Ai ai;
    private Piece piece;
    private Piece nextPiece;
    private long score;
    private boolean is3D;
    private boolean gameOver;
    private final int difficulty;

    public Game(boolean is3D, int difficulty, Runnable updateModelLambda) {
        this.is3D = is3D;
        this.difficulty = difficulty;
        this.updateModelLambda = updateModelLambda;
        resetGame();
    }

    public boolean is3D() {
        return is3D;
    }

    public Grid getGrid() {
        return grid;
    }

    public Piece getCurrentPiece() {
        return piece;
    }

    public Piece getNextPiece() {
        return nextPiece;
    }

    public long getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setRenderingMode(boolean is3D) {
        if (this.is3D != is3D) {
            this.is3D = is3D;
            resetGame();
        }
    }

    private void updateScore(int linesCleared) {
        score += linesCleared * 100L;
    }

    public void translateCurrentPiece(TetrisVector translation) {
        if (gameOver) return;
        var originalPos = new TetrisVector(piece.getPosition());

        piece.translate(translation);

        if (piece.getPosition().getX() < 0) {
            piece.getPosition().setX(0);
        }

        if (piece.getPosition().getX() + piece.getWidth() > grid.getWidth()) {
            piece.getPosition().setX(grid.getWidth() - piece.getWidth());
        }

        if (is3D) {
            if (piece.getPosition().getZ() < 0) {
                piece.getPosition().setZ(0);
            }

            if (piece.getPosition().getZ() + ((Piece3D) piece).getDepth() > grid.getDepth()) {
                piece.getPosition().setZ(grid.getDepth() - ((Piece3D) piece).getDepth());
            }
        }

        if (grid.checkCollision(piece)) {
            piece.setPosition(originalPos);

            if (translation.getY() < 0) {
                freeze();
            }
        }
    }

    private void freeze() {
        if (gameOver) return;
        grid.freezePiece(piece);

        int linesCleared = grid.clearFullLines();
        updateScore(linesCleared);

        generateNewPiece();

        if (grid.checkCollision(piece)) {
            Logger.getLogger(Game.class.getName()).log(Level.INFO, "Collision detected, game over!");
            gameOver = true;
        }
    }

    public void resetGame() {
        if (future != null) {
            future.cancel(false);
        }

        if (ai != null) {
            ai.shutdown();
        }

        grid = Grid.create(new TetrisVector(new int[]{Consts.GRID_WIDTH, Consts.GRID_HEIGHT, Consts.GRID_DEPTH}), is3D);
        ai = new Ai(grid, is3D ? AIParameters.DEFAULT_3D : AIParameters.DEFAULT);
        piece = null;
        nextPiece = null;
        generateNewPiece();
        score = 0;
        gameOver = false;

        future = executor.scheduleAtFixedRate(
                () -> {
                    if (is3D) {
                        translateCurrentPiece(new TetrisVector(new int[]{0, -1, 0}));
                    } else {
                        translateCurrentPiece(new TetrisVector(new int[]{0, -1}));
                    }
                    updateModelLambda.run();
                }, 0, 200 / difficulty, TimeUnit.MILLISECONDS
        );
    }

    private void generateNewPiece() {
        if (gameOver) return;

        if (nextPiece == null) {
            if (is3D) {
                nextPiece = PieceGenerator.generate3DPiece(grid.getWidth(), grid.getHeight(), grid.getDepth());
            } else {
                nextPiece = PieceGenerator.generatePiece2D(grid.getWidth(), grid.getHeight());
            }
        }

        piece = nextPiece;
        if (is3D) {
            nextPiece = PieceGenerator.generate3DPiece(grid.getWidth(), grid.getHeight(), grid.getDepth());
        } else {
            nextPiece = PieceGenerator.generatePiece2D(grid.getWidth(), grid.getHeight());
        }
    }

    public void rotateCurrentPiece() {
        if (gameOver) return;
        assert piece instanceof Piece2D : "Current piece is not a 2D piece";
        ((Piece2D) piece).rotate2d(piece -> grid.checkCollision(piece));
    }

    public void rotateCurrentPiece3D(RotationAxis axis, boolean reverse) {
        if (gameOver) return;
        assert piece instanceof Piece3D : "Current piece is not a 3D piece";
        ((Piece3D) piece).rotate3D(axis, (piece) -> grid.checkCollision(piece), reverse);
    }

    public void runAi() {
        if (gameOver) return;
        ai.makeMove(piece, nextPiece);
        updateScore(grid.clearFullLines());
        generateNewPiece();
    }

    public void cleanup() {
        ai.shutdown();
        if (future != null) {
            future.cancel(false);
        }
        executor.shutdown();
    }
}
