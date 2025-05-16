package fr.polytech.pie.model.twoD;

import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.CurrentPiece;

import java.util.function.Predicate;

public class CurrentPiece2D extends CurrentPiece {
    private Piece[][] piece;

    public CurrentPiece2D(boolean[][] piece, int x, int y) {
        super(x, y);
        this.piece = new Piece[piece.length][piece[0].length];
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (piece[i][j]) {
                    this.piece[i][j] = color;
                } else {
                    this.piece[i][j] = Piece.Empty;
                }
            }
        }
    }

    public CurrentPiece2D(Piece[][] newPiece, int x, int y, Piece color) {
        super(x, y, color);
        this.piece = newPiece;
    }


    public Piece[][] getPiece2d() {
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
        Piece[][] rotatedPiece = new Piece[getWidth()][getHeight()];

        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                rotatedPiece[getWidth() - 1 - i][j] = piece[j][i];
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
        Piece[][] newPiece = new Piece[piece.length][];
        for (int i = 0; i < piece.length; i++) {
            newPiece[i] = piece[i].clone();
        }
        return new CurrentPiece2D(newPiece, x, y, color);
    }

}