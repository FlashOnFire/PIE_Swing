package fr.polytech.pie.model;

public abstract class Piece implements Cloneable {
    protected Position position;

    public Piece(Position position, PieceColor color) {
        this.position = position;
        this.color = color;
    }

    public PieceColor getColor() {
        return color;
    }

    protected PieceColor color;

    public Piece(Position position) {
        this.position = position;
        this.color = PieceColor.values()[(int) (Math.random() * PieceColor.values().length)];
        color = color == PieceColor.Empty ? PieceColor.Blue : color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public void translate(Position addedPosition) {
        this.position.add(addedPosition);
    }

    @Override
    public Piece clone() {
        try {
            Piece clone = (Piece) super.clone();
            clone.position = this.position.clone();
            clone.color = this.color;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}