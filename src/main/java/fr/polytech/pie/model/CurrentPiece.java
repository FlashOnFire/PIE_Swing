package fr.polytech.pie.model;

import java.util.function.Predicate;

public abstract class CurrentPiece {
    protected int x;
    protected int y;

    public CurrentPiece(int x, int y) {
        this.x = x;
        this.y = y;
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

    public abstract int getHeight();
    public abstract int getWidth();
    public abstract boolean checkCollision(Predicate<CurrentPiece> collisionChecker);
}