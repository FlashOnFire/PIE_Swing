package fr.polytech.pie.model;

import java.util.function.Predicate;

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

    public void rotate(Predicate<CurrentPiece> collisionCheck) {
        int width = getWidth();
        int height = getHeight();

        boolean[][] rotatedPiece = new boolean[width][height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                rotatedPiece[j][height - 1 - i] = piece[i][j];
            }
        }

        int oldX = x;
        if (height == 4) {
            x -= 1;
        } else if (width == 4) {
            x += 1;
        }

        CurrentPiece rotatedCurrentPiece = new CurrentPiece(rotatedPiece, x, y);

        if (!collisionCheck.test(rotatedCurrentPiece)) {
            piece = rotatedPiece;
        } else {
            x = oldX;
        }
    }

    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }
}
