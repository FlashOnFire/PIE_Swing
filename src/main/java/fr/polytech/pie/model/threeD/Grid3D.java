package fr.polytech.pie.model.threeD;

import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.Grid;
import fr.polytech.pie.model.Piece;

import java.util.Arrays;

public class Grid3D extends Grid {
    private final Piece[][][] grid;
    private final int depth;
    private final int[][] heightCache;

    public Grid3D(int width, int height, int depth) {
        super(width, height);
        this.depth = depth;
        this.grid = new Piece[depth][height][width];
        this.heightCache = new int[width][depth];

        for (int z = 0; z < depth; z++) {
            for (int y = 0; y < height; y++) {
                Arrays.fill(grid[z][y], Piece.Empty);
            }
        }
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public Piece getValue(int x, int y) {
        // Return true if any cell in the z-axis is filled
        for (int z = 0; z < depth; z++) {
            if (getValue(x, y, z) != Piece.Empty) {
                return getValue(x, y, z);
            }
        }
        return Piece.Empty;
    }

    public Piece getValue(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
            return Piece.Empty;
        }
        return grid[z][y][x];
    }

    // TODO: remove this nonsense
    @Override
    public void setValue(int x, int y, Piece value) {
        // Set all cells in the z-axis to the given value
        for (int z = 0; z < depth; z++) {
            setValue(x, y, z, value);
        }
    }

    public void setValue(int x, int y, int z, Piece value) {
        if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth) {
            Piece oldValue = grid[z][y][x];
            grid[z][y][x] = value;

            if ((oldValue == Piece.Empty && value != Piece.Empty) ||
                    (oldValue != Piece.Empty && value == Piece.Empty)) {
                updateHeightCache(x, z);
            }
        }
    }

    @Override
    public void freezePiece(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece3D)) {
            throw new IllegalArgumentException("Expected CurrentPiece3D but got " + currentPiece.getClass().getName());
        }

        Piece[][][] voxelGrid = ((CurrentPiece3D) currentPiece).getPiece3d();
        int pieceX = currentPiece.getX();
        int pieceY = currentPiece.getY();
        int pieceZ = ((CurrentPiece3D) currentPiece).getZ();

        for (int i = 0; i < currentPiece.getWidth(); i++) {
            for (int j = 0; j < currentPiece.getHeight(); j++) {
                for (int k = 0; k < ((CurrentPiece3D) currentPiece).getDepth(); k++) {
                    if (voxelGrid[k][j][i] != Piece.Empty) {
                        int x = pieceX + i;
                        int y = pieceY + j;
                        int z = pieceZ + k;
                        setValue(x, y, z, currentPiece.getColor());
                    }
                }
            }
        }
    }

    @Override
    public void removePiece(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece3D piece3D)) {
            throw new IllegalArgumentException("Expected CurrentPiece3D but got " + currentPiece.getClass().getName());
        }

        for (int i = 0; i < currentPiece.getWidth(); i++) {
            for (int j = 0; j < currentPiece.getHeight(); j++) {
                for (int k = 0; k < piece3D.getDepth(); k++) {
                    if (piece3D.getPiece3d()[k][j][i] != Piece.Empty) {
                        int x = currentPiece.getX() + i;
                        int y = currentPiece.getY() + j;
                        int z = piece3D.getZ() + k;
                        setValue(x, y, z, Piece.Empty);
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

        Piece[][][] voxelGrid = piece3D.getPiece3d();
        int pieceX = piece3D.getX();
        int pieceY = piece3D.getY();
        int pieceZ = piece3D.getZ();

        for (int i = 0; i < piece3D.getWidth(); i++) {
            for (int j = 0; j < piece3D.getHeight(); j++) {
                for (int k = 0; k < piece3D.getDepth(); k++) {
                    if (voxelGrid[k][j][i] != Piece.Empty) {
                        int x = pieceX + i;
                        int y = pieceY + j;
                        int z = pieceZ + k;

                        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
                            return true;
                        }

                        if (getValue(x, y, z) != Piece.Empty) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int clearFullLines(boolean dry) {
        int linesCleared = 0;

        // Check for full horizontal planes (XZ planes)
        for (int y = 0; y < height; y++) {
            boolean fullPlane = true;

            // Check if the plane is full
            for (int x = 0; x < width && fullPlane; x++) {
                for (int z = 0; z < depth; z++) {
                    if (grid[z][y][x] == Piece.Empty) {
                        fullPlane = false;
                        break;
                    }
                }
            }

            if (fullPlane) {
                linesCleared++;

                if (!dry) {
                    // Shift all planes above down
                    for (int i = y; i < height - 1; i++) {
                        for (int x = 0; x < width; x++) {
                            for (int z = 0; z < depth; z++) {
                                grid[z][i][x] = grid[z][i + 1][x];
                            }
                        }
                    }

                    // Clear the top plane
                    for (int x = 0; x < width; x++) {
                        for (int z = 0; z < depth; z++) {
                            grid[z][height - 1][x] = Piece.Empty;
                        }
                    }

                    y--; // Check the same line again after shifting
                }
            }
        }

        return linesCleared;
    }

    @Override
    public int clearFullLines() {
        int linesCleared = clearFullLines(false);
        if (linesCleared > 0) {
            recalculateAllHeights();
        }
        return linesCleared;
    }

    @Override
    public Grid copy() {
        Grid3D copy = new Grid3D(width, height, depth);
        for (int z = 0; z < depth; z++) {
            for (int y = 0; y < height; y++) {
                System.arraycopy(grid[z][y], 0, copy.grid[z][y], 0, width);
            }
        }

        // Copy the height cache too
        for (int x = 0; x < width; x++) {
            System.arraycopy(heightCache[x], 0, copy.heightCache[x], 0, depth);
        }

        return copy;
    }

    @Override
    public int getHoles() {
        int holes = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                boolean foundBlock = false;
                for (int y = height - 1; y >= 0; y--) {
                    if (grid[z][y][x] != Piece.Empty) {
                        foundBlock = true;
                    } else if (foundBlock) {
                        holes++;
                    }
                }
            }
        }
        return holes;
    }

    private void recalculateAllHeights() {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                updateHeightCache(x, z);
            }
        }
    }

    public int getHeightOfColumn3D(int x, int z) {
        return heightCache[x][z];
    }

    private void updateHeightCache(int x, int z) {
        for (int y = this.height - 1; y >= 0; y--) {
            if (grid[z][y][x] != Piece.Empty) {
                heightCache[x][z] = y + 1;
                return;
            }
        }
        heightCache[x][z] = 0;
    }
}