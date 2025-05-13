package fr.polytech.pie.model;

public class CurrentPiece {
    private boolean[][] piece;
    private int x;
    private int y;

    public CurrentPiece(boolean[][] piece, int x, int y) {
        this.piece = piece;
        this.x = x;
        this.y = y;
    }

    public boolean[][] getPiece() {
        return piece;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return piece[0].length;
    }

    public int getHeight() {
        return piece.length;
    }

    public void rotate() {
        int width = getWidth();
        int height = getHeight();
        boolean[][] rotatedPiece = new boolean[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                rotatedPiece[i][j] = piece[height - j - 1][i];
            }
        }

        this.piece = rotatedPiece;
    }

    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }
}
