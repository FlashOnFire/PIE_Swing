package fr.polytech.pie.model.threeD;

import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.RotationAxis;

import java.util.function.Predicate;

public class CurrentPiece3D extends CurrentPiece {
    private Piece[][][] piece;
    private int z;

    public CurrentPiece3D(boolean[][][] piece, int x, int y, int z) {
        super(x, y);
        this.z = z;
        this.piece = new Piece[piece.length][piece[0].length][piece[0][0].length];
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                for (int k = 0; k < piece[i][j].length; k++) {
                    if (piece[i][j][k]) {
                        this.piece[i][j][k] = color;
                    } else {
                        this.piece[i][j][k] = Piece.Empty;
                    }
                }
            }
        }
    }

    public CurrentPiece3D(Piece[][][] newGrid, int x, int y, int z, Piece color) {
        super(x, y, color);
        this.piece = newGrid;
        this.z = z;
    }

    public Piece[][][] getPiece3d() {
        return piece;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getDepth() {
        return piece.length;
    }

    @Override
    public int getWidth() {
        return piece[0][0].length;
    }

    @Override
    public int getHeight() {
        return piece[0].length;
    }

    public void translate3D(int dx, int dy, int dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }

    public void rotate3D(RotationAxis axis, Predicate<CurrentPiece> collisionChecker) {
        CurrentPiece3D original = copy();

        switch (axis) {
            case X -> rotate((d, h, w) -> new int[]{d, w, getHeight() - 1 - h}, getDepth(), getWidth(), getHeight());
            case Y -> rotate((d, h, w) -> new int[]{w, h, getDepth() - 1 - d}, getWidth(), getHeight(), getDepth());
            case Z -> rotate((d, h, w) -> new int[]{getWidth() - 1 - w, d, h}, getWidth(), getDepth(), getHeight());
        }

        if (collisionChecker.test(this)) {
            piece = original.getPiece3d();
        }
    }

    private void rotate(TriFunction<Integer, Integer, Integer, int[]> transform, int newD, int newH, int newW) {
        Piece[][][] rotated = new Piece[newD][newH][newW];

        for (int d = 0; d < getDepth(); d++) {
            for (int h = 0; h < getHeight(); h++) {
                for (int w = 0; w < getWidth(); w++) {
                    if (piece[d][h][w] != Piece.Empty) {
                        int[] coords = transform.apply(d, h, w);
                        rotated[coords[0]][coords[1]][coords[2]] = color;
                    }
                }
            }
        }

        piece = rotated;
    }

    public CurrentPiece3D copy() {
        Piece[][][] newGrid = new Piece[piece.length][][];
        for (int i = 0; i < piece.length; i++) {
            newGrid[i] = new Piece[piece[i].length][];
            for (int j = 0; j < piece[i].length; j++) {
                newGrid[i][j] = piece[i][j].clone();
            }
        }
        return new CurrentPiece3D(newGrid, x, y, z, color);
    }

    @FunctionalInterface
    private interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}
