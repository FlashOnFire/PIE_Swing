package fr.polytech.pie.model;

import java.util.function.Predicate;

public class CurrentPiece2D implements CurrentPiece {
    private boolean[][] piece;
    private int x;
    private int y;

    public CurrentPiece2D(boolean[][] piece, int x, int y) {
        this.piece = piece;
        this.x = x;
        this.y = y;
    }

    public boolean[][] getPiece2d() {
        return piece;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getWidth() {
        return piece[0].length;
    }

    @Override
    public int getHeight() {
        return piece.length;
    }

    public void translate2d(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void rotate2d(Predicate<CurrentPiece2D> collisionChecker) {
        // Save the original piece in case rotation causes a collision
        var original = copy();

        // Rotate the piece 90 degrees clockwise
        int width = getWidth();
        int height = getHeight();
        boolean[][] rotatedPiece = new boolean[width][height];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                rotatedPiece[j][height - 1 - i] = piece[i][j];
            }
        }

        // Update the piece
        piece = rotatedPiece;

        // Check if the rotation causes a collision
        if (collisionChecker.test(this)) {
            // Restore the original piece if there's a collision
            piece = original.getPiece2d();
        }
    }

    public CurrentPiece2D copy() {
        boolean[][] newPiece = new boolean[piece.length][];
        for (int i = 0; i < piece.length; i++) {
            newPiece[i] = piece[i].clone();
        }
        return new CurrentPiece2D(newPiece, x, y);
    }
}