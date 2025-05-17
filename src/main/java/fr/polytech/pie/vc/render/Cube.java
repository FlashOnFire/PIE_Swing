package fr.polytech.pie.vc.render;

import org.joml.Vector3f;

public class Cube {
    private final Vector3f position;
    private final Vector3f color;

    public Cube(Vector3f position, Vector3f color) {
        this.position = position;
        this.color = color;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getColor() {
        return color;
    }
}
