package fr.polytech.pie.model.threeD;

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
        int depth = getDepth();
        int height = getHeight();
        int width = getWidth();
        boolean[][][] rotated = new boolean[depth][width][height];

        for (int d = 0; d < depth; d++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    rotated[d][w][height - 1 - h] = piece[d][h][w];
                }
            }
        }

        piece = rotated;
    }

    private void rotateAroundY() {
        int depth = getDepth();
        int height = getHeight();
        int width = getWidth();
        boolean[][][] rotated = new boolean[width][height][depth];

        for (int d = 0; d < depth; d++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    rotated[w][h][depth - 1 - d] = piece[d][h][w];
                }
            }
        }

        piece = rotated;
    }

    private void rotateAroundZ() {
        int depth = getDepth();
        int height = getHeight();
        int width = getWidth();
        boolean[][][] rotated = new boolean[depth][width][height];

        for (int d = 0; d < depth; d++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    rotated[d][w][height - 1 - h] = piece[d][h][w];
                }
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