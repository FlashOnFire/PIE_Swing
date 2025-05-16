package fr.polytech.pie.model.threeD;

import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.Grid;

public class Grid3D extends Grid {
    private final Piece[][][] grid;
    private final int depth;

    public Grid3D(int width, int height, int depth) {
        super(width, height);
        this.depth = depth;
        this.grid = new Piece[depth][height][width];
        for (int z = 0; z < depth; z++) {
            for (int y = 0; y < height; y++) {
                java.util.Arrays.fill(grid[z][y], Piece.Empty);
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

    @Override
    public void setValue(int x, int y, Piece value) {
        // Set all cells in the z-axis to the given value
        for (int z = 0; z < depth; z++) {
            setValue(x, y, z, value);
        }
    }

    public void setValue(int x, int y, int z, Piece value) {
        if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth) {
            grid[z][y][x] = value;
        }
    }

    @Override
    public void freezePiece(CurrentPiece currentPiece) {
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
                        setValue(x, y, z, currentPiece.getColor());
                    }
                }
            }
        }
    }

    @Override
    public void removePiece(CurrentPiece possibility) {
        if (!(possibility instanceof CurrentPiece3D piece3D)) {
            throw new IllegalArgumentException("Expected CurrentPiece3D but got " + possibility.getClass().getName());
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
    public int clearFullLines() {
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

        return linesCleared;
    }

    @Override
    public int countFullLines() {
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
            }
        }

        return linesCleared;
    }

    public int getHeightOfColumn3D(int x, int z) {
        int height = 0;
        for (int y = 0; y < depth; y++) {
            if (grid[z][y][x] != Piece.Empty) {
                height = depth - y;
                break;
            }
        }
        return height;
    }
}