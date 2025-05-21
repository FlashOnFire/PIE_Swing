package fr.polytech.pie.model.twoD;

import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.PieceColor;
import fr.polytech.pie.model.TetrisVector;

import java.util.function.Predicate;

public class Piece2D extends Piece {
    private PieceColor[][] pieceColor;

    public Piece2D(boolean[][] piece, TetrisVector position) {
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
        var original = clone();

        PieceColor[][] rotatedPieceColor = new PieceColor[getWidth()][getHeight()];

        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                rotatedPieceColor[getWidth() - 1 - i][j] = pieceColor[j][i];
            }
        }

        pieceColor = rotatedPieceColor;

        TetrisVector[] wallKick = {
                new TetrisVector(new int[]{-1, 0}),
                new TetrisVector(new int[]{-1, 1}),
                new TetrisVector(new int[]{0, -2}),
                new TetrisVector(new int[]{1, -2})
        };
        TetrisVector[] iWallKick = {
                new TetrisVector(new int[]{-2, 0}),
                new TetrisVector(new int[]{1, 0}),
                new TetrisVector(new int[]{-2, -1}),
                new TetrisVector(new int[]{1, 2})
        };
        if (getWidth() == 4 || getHeight() == 4) {
            if (getWidth() == 4) {
                position.subtract(new TetrisVector(new int[]{1, 0}));
            } else if (getHeight() == 4) {
                position.add(new TetrisVector(new int[]{1, 0}));
            }
            wallKick = iWallKick;
        }

        if (collisionChecker.test(this)) {
            for (TetrisVector tetrisVector : wallKick) {
                this.position.add(tetrisVector);
                if (!collisionChecker.test(this)) {
                    break;
                }
                this.position.subtract(tetrisVector);
            }
        }


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
