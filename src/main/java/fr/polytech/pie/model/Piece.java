package fr.polytech.pie.model;

public abstract class Piece implements Cloneable {
    protected TetrisVector position;

    public PieceColor getColor() {
        return color;
    }

    protected PieceColor color;

    public Piece(TetrisVector position) {
        this.position = position;
        this.color = PieceColor.values()[(int) (Math.random() * (PieceColor.values().length - 2) + 1)]; // Skip empty and preview pieces
    }

    public TetrisVector getPosition() {
        return position;
    }

    public void setPosition(TetrisVector position) {
        this.position = position;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public void translate(TetrisVector addedPosition) {
        this.position.add(addedPosition);
    }

    @Override
    public Piece clone() {
        try {
            Piece clone = (Piece) super.clone();
            clone.position = new TetrisVector(this.position);
            clone.color = this.color;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}