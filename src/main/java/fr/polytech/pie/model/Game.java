package fr.polytech.pie.model;

import fr.polytech.pie.Consts;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Game {
    private Grid grid;
    private Ai ai = new Ai(grid);
    private CurrentPiece currentPiece;
    private int score;
    private boolean is3D;

    public Game(boolean is3D) {
        this.is3D = is3D;
        if (is3D) {
            this.grid = new Grid(Consts.GRID_WIDTH, Consts.GRID_HEIGHT, Consts.GRID_DEPTH, true);
            this.currentPiece = PieceGenerator.generatePiece(grid.getWidth(), true);
        } else {
            this.grid = new Grid(Consts.GRID_WIDTH, Consts.GRID_HEIGHT);
            this.currentPiece = PieceGenerator.generatePiece(grid.getWidth(), false);
        }
        this.score = 0;
    }

    public Grid getGrid() {
        return grid;
    }

    public CurrentPiece getCurrentPiece() {
        return currentPiece;
    }

    public int getScore() {
        return score;
    }

    public boolean is3D() {
        return is3D;
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
        assert currentPiece instanceof CurrentPiece2D : "Current piece is not a 2D piece";
        int originalX = currentPiece.getX();
        int originalY = currentPiece.getY();

        ((CurrentPiece2D)currentPiece).translate2d(dx, dy);

        if (currentPiece.getX() < 0) {
            currentPiece.setX(0);
        }

        if (currentPiece.getX() + currentPiece.getWidth() > grid.getWidth()) {
            currentPiece.setX(grid.getWidth() - currentPiece.getWidth());
        }

        if (grid.checkCollision(currentPiece)) {
            currentPiece.setX(originalX);
            currentPiece.setY(originalY);

            if (dy > 0) {
                grid.freezePiece(currentPiece);

                int linesCleared = grid.clearFullLines();
                if (linesCleared > 0) {
                    updateScore(linesCleared);
                }

                generateNewPiece();

                if (grid.checkCollision(currentPiece)) {
                    Logger.getLogger(Game.class.getName()).log(Level.INFO, "Collision detected, game over!");
                    resetGame();
                }
            }
        }
    }

    public void translateCurrentPiece3D(int dx, int dy, int dz) {
        if (!is3D || !(currentPiece instanceof CurrentPiece3D piece3D)) {
            return;
        }

        int originalX = piece3D.getX();
        int originalY = piece3D.getY();
        int originalZ = piece3D.getZ();

        piece3D.translate3D(dx, dy, dz);

        // Check boundaries for x and y
        if (piece3D.getX() < 0) {
            piece3D.setX(0);
        }

        if (piece3D.getX() + piece3D.getWidth() > grid.getWidth()) {
            piece3D.setX(grid.getWidth() - piece3D.getWidth());
        }

        // Check z boundaries
        if (piece3D.getZ() < 0) {
            piece3D.setZ(0);
        }

        if (piece3D.getZ() + piece3D.getDepth() > grid.getDepth()) {
            piece3D.setZ(grid.getDepth() - piece3D.getDepth());
        }

        // Handle collision
        if (grid.checkCollision(piece3D)) {
            piece3D.setX(originalX);
            piece3D.setY(originalY);
            piece3D.setZ(originalZ);

            if (dy > 0) {
                grid.freezePiece(piece3D);

                int linesCleared = grid.clearFullLines();
                if (linesCleared > 0) {
                    updateScore(linesCleared);
                }

                generateNewPiece();

                if (grid.checkCollision(currentPiece)) {
                    Logger.getLogger(Game.class.getName()).log(Level.INFO, "Collision detected, game over!");
                    resetGame();
                }
            }
        }
    }

    public void resetGame() {
        if (is3D) {
            grid = new Grid(Consts.GRID_WIDTH, Consts.GRID_HEIGHT, Consts.GRID_DEPTH, true);
            currentPiece = PieceGenerator.generatePiece(grid.getWidth(), true);
        } else {
            grid = new Grid(Consts.GRID_WIDTH, Consts.GRID_HEIGHT);
            currentPiece = PieceGenerator.generatePiece(grid.getWidth(), false);
        }
        ai = new Ai(grid);
        score = 0;
    }

    private void generateNewPiece() {
        if (is3D) {
            currentPiece = PieceGenerator.generatePiece(grid.getWidth(), true);
        } else {
            currentPiece = PieceGenerator.generatePiece(grid.getWidth(), false);
        }
    }

    public void rotateCurrentPiece() {
        assert currentPiece instanceof CurrentPiece2D : "Current piece is not a 2D piece";        ((CurrentPiece2D)currentPiece).rotate2d((CurrentPiece2D piece) -> grid.checkCollision(piece));
    }

    public void rotateCurrentPiece3D(RotationAxis axis) {
        if (!is3D || !(currentPiece instanceof CurrentPiece3D piece3D)) {
            return;
        }

        piece3D.rotate3D(axis, (piece) -> grid.checkCollision(piece));
    }

    public void runAi() {
        ai.makeMove(currentPiece);
        if (is3D) {
            this.currentPiece = PieceGenerator.generatePiece(grid.getWidth(), true);
        } else {
            this.currentPiece = PieceGenerator.generatePiece(grid.getWidth(), false);
        }
    }
}
