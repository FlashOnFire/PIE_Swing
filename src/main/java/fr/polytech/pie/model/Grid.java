package fr.polytech.pie.model;

import fr.polytech.pie.model.twoD.Grid2D;
import fr.polytech.pie.model.threeD.Grid3D;

import java.util.Set;

public abstract class Grid {
    protected final Position size;

    public Grid(Position size) {
        this.size = size;
    }

    public int getWidth() {
        return size.getX();
    }

    public int getHeight() {
        return size.getY();
    }

    public int getDepth() {
        return size.getZ();
    }

    @SuppressWarnings("unused")
    public abstract void freezePiece(Piece piece);

    public abstract void removePiece(Piece possibility);

    public abstract boolean checkCollision(Piece piece);

    public abstract int clearFullLines(boolean dry);

    public abstract int clearFullLines();

    public abstract PieceColor getValue(Position position);

    public static Grid create(Position size, boolean is3D) {
        return is3D ? new Grid3D(size) : new Grid2D(size);
    }

    public abstract Grid copy();

    public abstract int getHoles();

    public abstract Set<Piece> getPiecesPossibilities(Piece piece);


    public boolean isOutOfBounds(Position position) {
        for (int i = 0; i < position.getSize(); i++) {
            if (position.getPositions()[i] < 0 || position.getPositions()[i] >= size.getPositions()[i]) {
                return true;
            }
        }
        return false;
    }
}