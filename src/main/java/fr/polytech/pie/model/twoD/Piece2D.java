package fr.polytech.pie.model.twoD;

import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.PieceColor;
import fr.polytech.pie.model.Position;

import java.util.function.Predicate;

public class Piece2D extends Piece {
    private PieceColor[][] pieceColor;

    public Piece2D(boolean[][] piece, Position position) {
        super(position);
        this.pieceColor = new PieceColor[piece.length][piece[0].length];
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (piece[i][j]) {
                    this.pieceColor[i][j] = color;
                } else {
                    this.pieceColor[i][j] = PieceColor.Empty;
                }
            }
        }
    }


    public PieceColor[][] getPiece2d() {
        return pieceColor;
    }

    @Override
    public int getWidth() {
        return pieceColor[0].length;
    }

    @Override
    public int getHeight() {
        return pieceColor.length;
    }

    public void rotate2d(Predicate<Piece> collisionChecker) {
        // Save the original piece in case rotation causes a collision
        var original = clone();

        // Rotate the piece 90 degrees clockwise
        PieceColor[][] rotatedPieceColor = new PieceColor[getWidth()][getHeight()];

        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                rotatedPieceColor[getWidth() - 1 - i][j] = pieceColor[j][i];
            }
        }

        // Update the piece
        pieceColor = rotatedPieceColor;

        // Check if the rotation causes a collision
        if (collisionChecker.test(this)) {
            pieceColor = original.getPiece2d();
        }
    }

    @Override
    public Piece2D clone() {
        Piece2D clone = (Piece2D) super.clone();

        clone.pieceColor = new PieceColor[pieceColor.length][];
        for (int i = 0; i < pieceColor.length; i++) {
            clone.pieceColor[i] = pieceColor[i].clone();
        }

        return clone;
    }
}