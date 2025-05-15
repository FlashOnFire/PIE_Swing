package fr.polytech.pie.model;

import fr.polytech.pie.model.TwoD.Grid2D;
import fr.polytech.pie.model.ThreeD.Grid3D;

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

    public abstract boolean getValue(int x, int y);
    public abstract void setValue(int x, int y, boolean value);
    public abstract void freezePiece(CurrentPiece currentPiece);
    public abstract void removePiece(CurrentPiece possibility);
    public abstract boolean checkCollision(CurrentPiece currentPiece);
    public abstract int clearFullLines();
    public abstract int countFullLines();

    public static Grid create(int width, int height, int depth, boolean is3D) {
        return is3D ? new Grid3D(width, height, depth) : new Grid2D(width, height);
    }

}