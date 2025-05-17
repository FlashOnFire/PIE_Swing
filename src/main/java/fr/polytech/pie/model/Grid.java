package fr.polytech.pie.model;

import fr.polytech.pie.model.twoD.Grid2D;
import fr.polytech.pie.model.threeD.Grid3D;

public abstract class Grid {
    protected final int width;
    protected final int height;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public abstract Piece getValue(int x, int y);
    @SuppressWarnings("unused")
    public abstract void setValue(int x, int y, Piece value);
    public abstract void freezePiece(CurrentPiece currentPiece);
    public abstract void removePiece(CurrentPiece possibility);
    public abstract boolean checkCollision(CurrentPiece currentPiece);
    public abstract int clearFullLines(boolean dry);
    public abstract int clearFullLines();

    public static Grid create(int width, int height, int depth, boolean is3D) {
        return is3D ? new Grid3D(width, height, depth) : new Grid2D(width, height);
    }

}