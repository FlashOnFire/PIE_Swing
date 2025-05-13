package fr.polytech.pie.model;

public class Grid {
    private final boolean[][] grid;
    private final int width;
    private final int height;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new boolean[height][width];
    }

    public boolean getValue(int x, int y) {
        return grid[y][x];
    }

    public void setValue(int x, int y, boolean value) {
        grid[y][x] = value;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void freezePiece(CurrentPiece currentPiece) {
        for (int i = 0; i < currentPiece.getHeight(); i++) {
            for (int j = 0; j < currentPiece.getWidth(); j++) {
                if (currentPiece.getPiece()[i][j]) {
                    int x = currentPiece.getX() + j;
                    int y = currentPiece.getY() + i;
                    setValue(x, y, true); // Freeze the piece in the grid
                }
            }
        }
    }

    public boolean checkCollision(CurrentPiece currentPiece) {
        for (int i = 0; i < currentPiece.getHeight(); i++) {
            for (int j = 0; j < currentPiece.getWidth(); j++) {
                if (currentPiece.getPiece()[i][j]) {
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
