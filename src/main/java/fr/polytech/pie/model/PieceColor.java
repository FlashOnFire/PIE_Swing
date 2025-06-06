package fr.polytech.pie.model;

import org.joml.Vector3f;

import java.awt.Color;

public enum PieceColor {
    Empty,
    Red,
    Green,
    Blue,
    Cyan,
    Magenta,
    Orange,
    Preview;

    public Color getColor() {
        return switch (this) {
            case Empty -> Color.WHITE;
            case Red -> Color.RED;
            case Green -> Color.GREEN;
            case Blue -> Color.BLUE;
            case Cyan -> Color.CYAN;
            case Magenta -> Color.MAGENTA;
            case Orange -> Color.ORANGE;
            case Preview -> Color.LIGHT_GRAY;
        };
    }

    public Vector3f getVector() {
        return new Vector3f(getColor().getRed() / 255f, getColor().getGreen() / 255f, getColor().getBlue() / 255f);
    }
}
