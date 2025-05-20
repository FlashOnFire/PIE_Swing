package fr.polytech.pie.model;

public abstract class CurrentPiece implements Cloneable {
    protected int x;
    protected int y;

    public CurrentPiece(int x, int y, Piece color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public Piece getColor() {
        return color;
    }

    protected Piece color;

    public CurrentPiece(int x, int y) {
        this.x = x;
        this.y = y;
        this.color = Piece.values()[(int) (Math.random() * (Piece.values().length -2) + 1)]; // Skip empty and preview pieces
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public abstract int getWidth();
    public abstract int getHeight();

    @Override
    public CurrentPiece clone() {
        try {
            CurrentPiece clone = (CurrentPiece) super.clone();
            clone.x = this.x;
            clone.y = this.y;
            clone.color = this.color;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}