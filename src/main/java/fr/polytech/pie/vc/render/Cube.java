package fr.polytech.pie.vc.render;

import org.joml.Vector3f;

public class Cube {
    private Vector3f position;
    private Vector3f color;

    public Cube(Vector3f position, Vector3f color) {
        this.position = position;
        this.color = color;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getColor() {
        return color;
    }
}
