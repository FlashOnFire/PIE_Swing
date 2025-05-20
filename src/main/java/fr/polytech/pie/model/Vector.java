package fr.polytech.pie.model;

import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Objects;

public class Vector {
    private final int size;
    private final int[] positions;

    public Vector(int size) {
        this.size = size;
        this.positions = new int[size];
    }

    public Vector(int[] positions) {
        this.size = positions.length;
        this.positions = new int[size];

        System.arraycopy(positions, 0, this.positions, 0, size);
    }

    public Vector(Vector position) {
        this.size = position.size;
        this.positions = new int[size];

        System.arraycopy(position.positions, 0, this.positions, 0, size);
    }

    public int[] getPositions() {
        return positions;
    }

    public int getSize() {
        return size;
    }

    public void setX(int x) {
        positions[0] = x;
    }

    public void setY(int y) {
        positions[1] = y;
    }

    public int getX() {
        return positions[0];
    }

    public int getY() {
        return positions[1];
    }

    public void setZ(int z) {
        if (size > 2) {
            positions[2] = z;
        } else {
            throw new IllegalArgumentException("Position does not have a Z coordinate");
        }
    }

    public int getZ() {
        if (size > 2) {
            return positions[2];
        } else {
            throw new IllegalArgumentException("Position does not have a Z coordinate");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vector position = (Vector) o;
        return size == position.size && Arrays.equals(positions, position.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(positions), size);
    }

    public void add(Vector other) {
        if (this.size != other.size) {
            throw new IllegalArgumentException("Positions must have the same size to be added");
        }
        for (int i = 0; i < size; i++) {
            positions[i] = positions[i] + other.positions[i];
        }
    }

    public Vector3f toVector3f() {
        if (size == 3) {
            return new Vector3f(positions[0], positions[1], positions[2]);
        } else {
            throw new IllegalArgumentException("Position does not have a Z coordinate");
        }
    }

    @Override
    public String toString() {
        return "Position{" +
                "positions=" + Arrays.toString(positions) +
                ", size=" + size +
                '}';
    }
}
