package fr.polytech.pie.model.twoD;

import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.Grid;

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
        for (int i = 0; i < currentPiece.getWidth(); i++) {
            for (int j = 0; j < currentPiece.getHeight(); j++) {
                if (piece[j][i]) {
                    int x = currentPiece.getX() + i;
                    int y = currentPiece.getY() + j;
                    setValue(x, y, true);
                }
            }
        }
    }

    @Override
    public void removePiece(CurrentPiece currentPiece) {
        for (int i = 0; i < currentPiece.getWidth(); i++) {
            for (int j = 0; j < currentPiece.getHeight(); j++) {
                if (((CurrentPiece2D) currentPiece).getPiece2d()[j][i]) {
                    int x = currentPiece.getX() + i;
                    int y = currentPiece.getY() + j;
                    setValue(x, y, false); // Remove the piece from the grid

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
        for (int i = 0; i < currentPiece.getWidth(); i++) {
            for (int j = 0; j < currentPiece.getHeight(); j++) {
                if (piece[j][i]) {
                    int x = currentPiece.getX() + i;
                    int y = currentPiece.getY() + j;

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

        for (int y = 0; y < height; y++) {
            boolean fullLine = true;
            for (int x = 0; x < width; x++) {
                if (!grid[y][x]) {
                    fullLine = false;
                    break;
                }
            }

            if (fullLine) {
                linesCleared++;
                // Shift all lines above down
                for (int i = y; i < height - 1; i++) {
                    System.arraycopy(grid[i + 1], 0, grid[i], 0, width);
                }
                y--; // Check the same line again after shifting
                for (int x = 0; x < width; x++) {
                    grid[height - 1][x] = false; // Clear the last line
                }
            }
        }

        return linesCleared;
    }

    @Override
    public int countFullLines() {
        int linesCounted = 0;

        for (int i = 0; i < height; i++) {
            boolean fullLine = true;
            for (int j = 0; j < width; j++) {
                assert grid != null;
                if (!grid[i][j]) {
                    fullLine = false;
                    break;
                }
            }

            if (fullLine) {
                linesCounted++;
            }
        }

        return linesCounted;
    }

    public int getHeightOfColumn2D(int x) {
        int height = 0;
        for (int y = this.height - 1; y >= 0; y--) {
            if (grid[y][x]) {
                height = y+1;
                break;
            }
        }

        return height;
    }
}