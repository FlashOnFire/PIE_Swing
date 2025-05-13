package fr.polytech.pie.model;

public class Grid {
    private final int[][] grid;

    public Grid(int size) {
        this.grid = new int[size][size];
    }

    public int getValue(int x, int y) {
        return grid[x][y];
    }

    public void setValue(int x, int y, int value) {
        grid[x][y] = value;
    }

    public int getSize() {
        return grid.length;
    }

    public void freezePiece(CurrentPiece currentPiece) {
        for (int i = 0; i < currentPiece.getHeight(); i++) {
            for (int j = 0; j < currentPiece.getWidth(); j++) {
                if (currentPiece.getPiece()[i][j]) {
                    int x = currentPiece.getX() + j;
                    int y = currentPiece.getY() + i;
                    setValue(x, y, 1); // Freeze the piece in the grid
                }
            }
        }
    }
}
