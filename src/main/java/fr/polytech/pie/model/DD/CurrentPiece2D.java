package fr.polytech.pie.model.DD;

import fr.polytech.pie.model.CurrentPiece;

import java.util.function.Predicate;

public class CurrentPiece2D extends CurrentPiece {
    private boolean[][] piece;

    public CurrentPiece2D(boolean[][] piece, int x, int y) {
        super(x, y);
        this.piece = piece;
    }

    public boolean[][] getPiece2d() {
        return piece;
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

    public void rotate2d(Predicate<CurrentPiece> collisionChecker) {
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

    @Override
    public boolean checkCollision(Predicate<CurrentPiece> collisionChecker) {
        return collisionChecker.test(this);
    }
}