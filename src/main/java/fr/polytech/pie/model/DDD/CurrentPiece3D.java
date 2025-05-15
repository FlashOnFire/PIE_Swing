package fr.polytech.pie.model.DDD;

import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.RotationAxis;

import java.util.function.Predicate;

public class CurrentPiece3D extends CurrentPiece {
    private boolean[][][] piece;
    private int z;

    public CurrentPiece3D(boolean[][][] piece, int x, int y, int z) {
        super(x, y);
        this.piece = piece;
        this.z = z;
    }

    public boolean[][][] getPiece3d() {
        return piece;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getDepth() {
        return piece[0][0].length;
    }

    @Override
    public int getWidth() {
        return piece[0].length;
    }

    @Override
    public int getHeight() {
        return piece.length;
    }

    public void translate3D(int dx, int dy, int dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }

    public void rotate3D(RotationAxis axis, Predicate<CurrentPiece> collisionChecker) {
        // Save the original voxel grid in case rotation causes a collision
        CurrentPiece3D original = copy();

        // Perform rotation based on the axis
        switch (axis) {
            case X -> rotateAroundX();
            case Y -> rotateAroundY();
            case Z -> rotateAroundZ();
        }

        // Check if the rotation causes a collision
        if (collisionChecker.test(this)) {
            // Restore the original voxel grid if there's a collision
            piece = original.getPiece3d();
        }
    }

    private void rotateAroundX() {
        int height = getHeight();
        int width = getWidth();
        int depth = getDepth();
        boolean[][][] rotated = new boolean[depth][width][height];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < depth; k++) {
                    rotated[depth - 1 - k][j][i] = piece[i][j][k];
                }
            }
        }

        piece = rotated;
    }

    private void rotateAroundY() {
        int height = getHeight();
        int width = getWidth();
        int depth = getDepth();
        boolean[][][] rotated = new boolean[height][depth][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < depth; k++) {
                    rotated[i][k][width - 1 - j] = piece[i][j][k];
                }
            }
        }

        piece = rotated;
    }

    private void rotateAroundZ() {
        int height = getHeight();
        int width = getWidth();
        int depth = getDepth();
        boolean[][][] rotated = new boolean[width][height][depth];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.arraycopy(piece[i][j], 0, rotated[j][height - 1 - i], 0, depth);
            }
        }

        piece = rotated;
    }

    public CurrentPiece3D copy() {
        boolean[][][] newGrid = new boolean[piece.length][][];
        for (int i = 0; i < piece.length; i++) {
            newGrid[i] = new boolean[piece[i].length][];
            for (int j = 0; j < piece[i].length; j++) {
                newGrid[i][j] = piece[i][j].clone();
            }
        }
        return new CurrentPiece3D(newGrid, x, y, z);
    }

    @Override
    public boolean checkCollision(Predicate<CurrentPiece> collisionChecker) {
        return collisionChecker.test(this);
    }
}