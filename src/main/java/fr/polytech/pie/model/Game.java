package fr.polytech.pie.model;

import fr.polytech.pie.Consts;
import fr.polytech.pie.model.twoD.Ai2D;
import fr.polytech.pie.model.twoD.CurrentPiece2D;
import fr.polytech.pie.model.twoD.Grid2D;
import fr.polytech.pie.model.threeD.Ai3D;
import fr.polytech.pie.model.threeD.CurrentPiece3D;
import fr.polytech.pie.model.threeD.Grid3D;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Game {
    private Grid grid;
    private Ai ai;
    private CurrentPiece currentPiece;
    private CurrentPiece nextPiece;
    private int score;
    private boolean is3D;
    private boolean gameOver;
    private int difficulty = 1;

    public Game(boolean is3D) {
        this.is3D = is3D;
        resetGame();
    }

    public boolean is3D() {
        return is3D;
    }

    public Grid getGrid() {
        return grid;
    }

    public CurrentPiece getCurrentPiece() {
        return currentPiece;
    }

    public CurrentPiece getNextPiece() {
        return nextPiece;
    }

    public int getScore() {
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
        score += linesCleared * 100;
    }

    public void translateCurrentPiece2D(int dx, int dy) {
        if (gameOver) return;
        assert currentPiece instanceof CurrentPiece2D : "Current piece is not a 2D piece";
        int originalX = currentPiece.getX();
        int originalY = currentPiece.getY();

        ((CurrentPiece2D) currentPiece).translate2d(dx, dy);

        if (currentPiece.getX() < 0) {
            currentPiece.setX(0);
        }

        if (currentPiece.getX() + currentPiece.getWidth() > grid.getWidth()) {
            currentPiece.setX(grid.getWidth() - currentPiece.getWidth());
        }

        if (grid.checkCollision(currentPiece)) {
            currentPiece.setX(originalX);
            currentPiece.setY(originalY);

            if (dy < 0) {
                freeze();
            }
        }
    }

    private void freeze() {
        if (gameOver) return;
        grid.freezePiece(currentPiece);

        int linesCleared = grid.clearFullLines();
        updateScore(linesCleared);

        generateNewPiece();

        if (grid.checkCollision(currentPiece)) {
            Logger.getLogger(Game.class.getName()).log(Level.INFO, "Collision detected, game over!");
            gameOver = true;
        }
    }

    public void translateCurrentPiece3D(int dx, int dy, int dz) {
        if (gameOver) return;
        if (!is3D || !(currentPiece instanceof CurrentPiece3D piece3D)) {
            throw new IllegalArgumentException("Expected CurrentPiece2D but got " + currentPiece.getClass().getName());
        }

        int originalX = piece3D.getX();
        int originalY = piece3D.getY();
        int originalZ = piece3D.getZ();

        piece3D.translate3D(dx, dy, dz);

        if (piece3D.getX() < 0) {
            piece3D.setX(0);
        }

        if (piece3D.getX() + piece3D.getWidth() > grid.getWidth()) {
            piece3D.setX(grid.getWidth() - piece3D.getWidth());
        }

        if (piece3D.getZ() < 0) {
            piece3D.setZ(0);
        }

        if (piece3D.getZ() + piece3D.getDepth() > ((Grid3D) grid).getDepth()) {
            piece3D.setZ(((Grid3D) grid).getDepth() - piece3D.getDepth());
        }


        if (grid.checkCollision(piece3D)) {
            piece3D.setX(originalX);
            piece3D.setY(originalY);
            piece3D.setZ(originalZ);

            if (dy < 0) {
                freeze();
            }
        }
    }

    public void resetGame() {
        grid = Grid.create(Consts.GRID_WIDTH, Consts.GRID_HEIGHT, Consts.GRID_DEPTH, is3D);
        if (is3D) {
            ai = new Ai3D((Grid3D) grid);
        } else {
            ai = new Ai2D((Grid2D) grid);
        }
        currentPiece = null;
        nextPiece = null;
        generateNewPiece();
        score = 0;
        gameOver = false;
    }

    private void generateNewPiece() {
        if (gameOver) return;
        if (nextPiece == null) {
            if (is3D) {
                nextPiece = PieceGenerator.generate3DPiece(grid.getWidth(), grid.getHeight(), ((Grid3D) grid).getDepth());
            } else {
                nextPiece = PieceGenerator.generatePiece2D(grid.getWidth(), grid.getHeight());
            }
        }

        currentPiece = nextPiece;
        if (is3D) {
            nextPiece = PieceGenerator.generate3DPiece(grid.getWidth(), grid.getHeight(), ((Grid3D) grid).getDepth());
        } else {
            nextPiece = PieceGenerator.generatePiece2D(grid.getWidth(), grid.getHeight());
        }
    }

    public void rotateCurrentPiece() {
        if (gameOver) return;
        assert currentPiece instanceof CurrentPiece2D : "Current piece is not a 2D piece";
        ((CurrentPiece2D) currentPiece).rotate2d(piece -> grid.checkCollision(piece));
    }

    public void rotateCurrentPiece3D(RotationAxis axis) {
        if (gameOver) return;
        assert currentPiece instanceof CurrentPiece3D : "Current piece is not a 3D piece";
        ((CurrentPiece3D) currentPiece).rotate3D(axis, (piece) -> grid.checkCollision(piece));
    }

    public void runAi() {
        if (gameOver) return;
        ai.makeMove(currentPiece, nextPiece);
        updateScore(grid.clearFullLines());
        generateNewPiece();
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getDifficulty() {
        return difficulty;
    }
}
