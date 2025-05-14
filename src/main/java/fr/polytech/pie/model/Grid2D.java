package fr.polytech.pie.model;

public class Grid2D extends Grid {
    private final boolean[][] grid;

    public Grid2D(int width, int height) {
        super(width, height);
        this.grid = new boolean[height][width];
    }

    @Override
    public boolean getValue(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        return grid[y][x];
    }

    @Override
    public void setValue(int x, int y, boolean value) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            grid[y][x] = value;
        }
    }

    @Override
    public void freezePiece(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece2D)) {
            throw new IllegalArgumentException("Expected CurrentPiece2D but got " + currentPiece.getClass().getName());
        }

        boolean[][] piece = ((CurrentPiece2D) currentPiece).getPiece2d();
        for (int i = 0; i < currentPiece.getHeight(); i++) {
            for (int j = 0; j < currentPiece.getWidth(); j++) {
                if (piece[i][j]) {
                    int x = currentPiece.getX() + j;
                    int y = currentPiece.getY() + i;
                    setValue(x, y, true);
                }
            }
        }
    }

    @Override
    public boolean checkCollision(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece2D piece2D)) {
            throw new IllegalArgumentException("Expected CurrentPiece2D but got " + currentPiece.getClass().getName());
        }

        boolean[][] piece = piece2D.getPiece2d();
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

    @Override
    public int clearFullLines() {
        int linesCleared = 0;

        for (int i = height - 1; i >= 0; i--) {
            boolean fullLine = true;
            for (int j = 0; j < width; j++) {
                if (!grid[i][j]) {
                    fullLine = false;
                    break;
                }
            }

            if (fullLine) {
                linesCleared++;
                // Shift all lines above down
                for (int k = i; k > 0; k--) {
                    System.arraycopy(grid[k - 1], 0, grid[k], 0, width);
                }
                // Clear the top line
                for (int j = 0; j < width; j++) {
                    grid[0][j] = false;
                }
            }
        }

        return linesCleared;
    }
}