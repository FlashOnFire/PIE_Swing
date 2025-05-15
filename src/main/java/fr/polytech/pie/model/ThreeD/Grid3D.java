package fr.polytech.pie.model.ThreeD;

import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.Grid;

public class Grid3D extends Grid {
    private final boolean[][][] grid;
    private final int depth;

    public Grid3D(int width, int height, int depth) {
        super(width, height);
        this.depth = depth;
        this.grid = new boolean[height][width][depth];
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public boolean getValue(int x, int y) {
        // Return true if any cell in the z-axis is filled
        for (int z = 0; z < depth; z++) {
            if (getValue(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public boolean getValue(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
            return false;
        }
        return grid[y][x][z];
    }

    @Override
    public void setValue(int x, int y, boolean value) {
        // Set all cells in the z-axis to the given value
        for (int z = 0; z < depth; z++) {
            setValue(x, y, z, value);
        }
    }

    public void setValue(int x, int y, int z, boolean value) {
        if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth) {
            grid[y][x][z] = value;
        }
    }

    @Override
    public void freezePiece(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece3D)) {
            throw new IllegalArgumentException("Expected CurrentPiece3D but got " + currentPiece.getClass().getName());
        }

        CurrentPiece3D piece3D = (CurrentPiece3D) currentPiece;
        boolean[][][] voxelGrid = piece3D.getPiece3d();
        int pieceX = piece3D.getX();
        int pieceY = piece3D.getY();
        int pieceZ = piece3D.getZ();

        for (int i = 0; i < piece3D.getWidth(); i++) {
            for (int j = 0; j < piece3D.getHeight(); j++) {
                for (int k = 0; k < piece3D.getDepth(); k++) {
                    if (voxelGrid[i][j][k]) {
                        int x = pieceX + i;
                        int y = pieceY + j;
                        int z = pieceZ + k;
                        setValue(x, y, z, true);
                    }
                }
            }
        }
    }

    @Override
    public void removePiece(CurrentPiece possibility) {
        if (!(possibility instanceof CurrentPiece3D)) {
            throw new IllegalArgumentException("Expected CurrentPiece3D but got " + possibility.getClass().getName());
        }

        CurrentPiece3D piece3D = (CurrentPiece3D) possibility;
        boolean[][][] voxelGrid = piece3D.getPiece3d();
        int pieceX = piece3D.getX();
        int pieceY = piece3D.getY();
        int pieceZ = piece3D.getZ();

        for (int i = 0; i < piece3D.getWidth(); i++) {
            for (int j = 0; j < piece3D.getHeight(); j++) {
                for (int k = 0; k < piece3D.getDepth(); k++) {
                    if (voxelGrid[i][j][k]) {
                        int x = pieceX + j;
                        int y = pieceY + i;
                        int z = pieceZ + k;
                        setValue(x, y, z, false);
                    }
                }
            }
        }
    }

    @Override
    public boolean checkCollision(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece3D piece3D)) {
            throw new IllegalArgumentException("Expected CurrentPiece3D but got " + currentPiece.getClass().getName());
        }

        boolean[][][] voxelGrid = piece3D.getPiece3d();
        int pieceX = piece3D.getX();
        int pieceY = piece3D.getY();
        int pieceZ = piece3D.getZ();

        for (int i = 0; i < piece3D.getWidth(); i++) {
            for (int j = 0; j < piece3D.getHeight(); j++) {
                for (int k = 0; k < piece3D.getDepth(); k++) {
                    if (voxelGrid[i][j][k]) {
                        int x = pieceX + j;
                        int y = pieceY + i;
                        int z = pieceZ + k;

                        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
                            return true;
                        }

                        if (getValue(x, y, z)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int clearFullLines() {
        int linesCleared = 0;

        // Check for full horizontal planes (XZ planes)
        for (int y = height - 1; y >= 0; y--) {
            boolean fullPlane = true;

            // Check if the plane is full
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    if (!grid[y][x][z]) {
                        fullPlane = false;
                        break;
                    }
                }
                if (!fullPlane)
                    break;
            }

            if (fullPlane) {
                linesCleared++;
                // Shift all planes above down
                for (int k = y; k > 0; k--) {
                    for (int x = 0; x < width; x++) {
                        System.arraycopy(grid[k - 1][x], 0, grid[k][x], 0, depth);
                    }
                }
                // Clear the top plane
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        grid[0][x][z] = false;
                    }
                }
            }
        }

        return linesCleared;
    }

    @Override
    public int countFullLines() {
        int linesCounted = 0;

        // Check for full horizontal planes (XZ planes)
        for (int y = 0; y < height; y++) {
            boolean fullPlane = true;

            // Check if the plane is full
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    assert grid != null;
                    if (!grid[y][x][z]) {
                        fullPlane = false;
                        break;
                    }
                }
                if (!fullPlane)
                    break;
            }

            if (fullPlane) {
                linesCounted++;
            }
        }

        return linesCounted;
    }

    public int getHeightOfColumn3D(int i, int j) {
        int height = 0;
        for (int k = 0; k < depth; k++) {
            if (grid[k][i][j]) {
                height = depth - k;
                break;
            }
        }
        return height;
    }
}