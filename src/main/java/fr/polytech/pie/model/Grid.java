package fr.polytech.pie.model;

public class Grid {
    private final boolean[][] grid2D; // 2D grid
    private final boolean[][][] grid3D; // 3D grid (for 3D mode)
    private final int width;
    private final int height;
    private final int depth;
    private final boolean is3D;

    public Grid(int width, int height) {
        this(width, height, 1, false);
    }

    public Grid(int width, int height, int depth, boolean is3D) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.is3D = is3D;

        if (is3D) {
            this.grid2D = null;
            this.grid3D = new boolean[height][width][depth];
        } else {
            this.grid2D = new boolean[height][width];
            this.grid3D = null;
        }
    }

    public boolean getValue(int x, int y) {
        if (is3D) {
            // For 3D grid, return true if any cell in the z-axis is filled
            for (int z = 0; z < depth; z++) {
                if (getValue(x, y, z)) {
                    return true;
                }
            }
            return false;
        }
        assert grid2D != null;
        return grid2D[y][x];
    }

    public boolean getValue(int x, int y, int z) {
        if (!is3D) {
            throw new UnsupportedOperationException("Cannot get 3D value in 2D mode");
        }
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
            return false;
        }
        assert grid3D != null;
        return grid3D[y][x][z];
    }

    public void setValue(int x, int y, boolean value) {
        if (is3D) {
            // For 3D grid, set all cells in the z-axis to the given value
            for (int z = 0; z < depth; z++) {
                setValue(x, y, z, value);
            }
        } else {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                assert grid2D != null;
                grid2D[y][x] = value;
            }
        }
    }

    public void setValue(int x, int y, int z, boolean value) {
        if (!is3D) {
            throw new UnsupportedOperationException("Cannot set 3D value in 2D mode");
        }
        if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth) {
            assert grid3D != null;
            grid3D[y][x][z] = value;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public boolean is3D() {
        return is3D;
    }

    public void freezePiece(CurrentPiece currentPiece) {
        if (is3D && currentPiece instanceof CurrentPiece3D) {
            freezePiece3D((CurrentPiece3D) currentPiece);
        } else {
            // Handle 2D piece (or 3D piece in 2D mode)
            boolean[][] piece = ((CurrentPiece2D) currentPiece).getPiece2d();
            for (int i = 0; i < currentPiece.getHeight(); i++) {
                for (int j = 0; j < currentPiece.getWidth(); j++) {
                    if (piece[i][j]) {
                        int x = currentPiece.getX() + j;
                        int y = currentPiece.getY() + i;
                        setValue(x, y, true); // Freeze the piece in the grid
                    }
                }
            }
        }
    }

    private void freezePiece3D(CurrentPiece3D piece3D) {
        boolean[][][] voxelGrid = piece3D.getPiece3d();
        int pieceX = piece3D.getX();
        int pieceY = piece3D.getY();
        int pieceZ = piece3D.getZ();

        for (int i = 0; i < piece3D.getHeight(); i++) {
            for (int j = 0; j < piece3D.getWidth(); j++) {
                for (int k = 0; k < piece3D.getDepth(); k++) {
                    if (voxelGrid[i][j][k]) {
                        int x = pieceX + j;
                        int y = pieceY + i;
                        int z = pieceZ + k;
                        setValue(x, y, z, true);
                    }
                }
            }
        }
    }

    public void removePiece(CurrentPiece currentPiece) {
        for (int i = 0; i < currentPiece.getHeight(); i++) {
            for (int j = 0; j < currentPiece.getWidth(); j++) {
                if (currentPiece instanceof CurrentPiece3D) {
                    if (((CurrentPiece3D) currentPiece).getPiece3d()[i][j][0]) { // Assuming depth is 1 for 2D
                        int x = currentPiece.getX() + j;
                        int y = currentPiece.getY() + i;
                        setValue(x, y, false); // Remove the piece from the grid
                    }
                } else {
                    // Handle 2D piece (or 3D piece in 2D mode)
                    if (((CurrentPiece2D) currentPiece).getPiece2d()[i][j]) {
                        int x = currentPiece.getX() + j;
                        int y = currentPiece.getY() + i;
                        setValue(x, y, false); // Remove the piece from the grid
                    }
                }
            }
        }
    }

    public boolean checkCollision(CurrentPiece currentPiece) {
        if (is3D && currentPiece instanceof CurrentPiece3D) {
            return checkCollision3D((CurrentPiece3D) currentPiece);
        } else {
            // Handle 2D piece (or 3D piece in 2D mode)
            boolean[][] piece = ((CurrentPiece2D) currentPiece).getPiece2d();
            for (int i = 0; i < currentPiece.getHeight(); i++) {
                for (int j = 0; j < currentPiece.getWidth(); j++) {
                    if (piece[i][j]) {
                        int x = currentPiece.getX() + j;
                        int y = currentPiece.getY() + i;

                        if (x < 0 || x >= width || y < 0 || y >= height) {
                            return true;
                        }

                        if (getValue(x, y)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    private boolean checkCollision3D(CurrentPiece3D piece3D) {
        boolean[][][] voxelGrid = piece3D.getPiece3d();
        int pieceX = piece3D.getX();
        int pieceY = piece3D.getY();
        int pieceZ = piece3D.getZ();

        for (int i = 0; i < piece3D.getHeight(); i++) {
            for (int j = 0; j < piece3D.getWidth(); j++) {
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

    public int countFullLines() {
        int fullLines = 0;

        for (int i = 0; i < height; i++) {
            boolean fullLine = true;
            for (int j = 0; j < width; j++) {
                if (!grid[i][j]) {
                    fullLine = false;
                    break;
                }
            }

            if (fullLine) {
                fullLines++;
            }
        }

        return fullLines;
    }

    public int clearFullLines() {
        if (is3D) {
            return clearFullLines3D();
        } else {
            return clearFullLines2D();
        }
    }

    private int clearFullLines2D() {
        int linesCleared = 0;

        for (int i = height - 1; i >= 0; i--) {
            boolean fullLine = true;
            for (int j = 0; j < width; j++) {
                assert grid2D != null;
                if (!grid2D[i][j]) {
                    fullLine = false;
                    break;
                }
            }

            if (fullLine) {
                linesCleared++;
                // Shift all lines above down
                for (int k = i; k > 0; k--) {
                    assert grid2D != null;
                    System.arraycopy(grid2D[k - 1], 0, grid2D[k], 0, width);
                }
                // Clear the top line
                for (int j = 0; j < width; j++) {
                    grid2D[0][j] = false;
                }
            }
        }

        return linesCleared;
    }

    private int clearFullLines3D() {
        int linesCleared = 0;

        // Check for full horizontal planes (XZ planes)
        for (int y = height - 1; y >= 0; y--) {
            boolean fullPlane = true;

            // Check if the plane is full
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    assert grid3D != null;
                    if (!grid3D[y][x][z]) {
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
                        if (depth >= 0) {
                            assert grid3D != null;
                            System.arraycopy(grid3D[k - 1][x], 0, grid3D[k][x], 0, depth);
                        }
                    }
                }
                // Clear the top plane
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        grid3D[0][x][z] = false;
                    }
                }
            }
        }

        return linesCleared;
    }

    public int getHeightOfColumn(int i) {
        int height = 0;
        if (is3D){
            for (int j = 0; j < this.height; j++) {
                for (int k = 0; k < this.depth; k++) {
                    if (grid3D[j][i][k]) {
                        height++;
                    }
                }
            }
            return height;
        } else {
            for (int j = 0; j < this.height; j++) {
                if (grid2D[j][i]) {
                    height++;
                }
            }
            return height;
        }
    }
}
