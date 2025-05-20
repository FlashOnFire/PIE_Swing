package fr.polytech.pie.model;

public abstract class Piece implements Cloneable {
    protected Vector position;

    public Piece(Vector position, PieceColor color) {
        this.position = position;
        this.color = color;
    }

    public PieceColor getColor() {
        return color;
    }

    protected PieceColor color;

    public Piece(Vector position) {
        this.position = position;
        this.color = PieceColor.values()[(int) (Math.random() * (PieceColor.values().length - 2) + 1)]; // Skip empty and preview pieces
    }

    public Vector getPosition() {
        return position;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public void translate(Vector addedPosition) {
        this.position.add(addedPosition);
    }

    @Override
    public Piece clone() {
        try {
            Piece clone = (Piece) super.clone();
            clone.position = new Vector(this.position);
            clone.color = this.color;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}