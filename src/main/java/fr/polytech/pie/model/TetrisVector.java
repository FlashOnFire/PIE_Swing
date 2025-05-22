package fr.polytech.pie.model;

import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Objects;

public class TetrisVector {
    private final int size;
    private final int[] vector;

    public TetrisVector(int size) {
        this.size = size;
        this.vector = new int[size];
    }

    public TetrisVector(int[] vector) {
        this.size = vector.length;
        this.vector = new int[size];

        System.arraycopy(vector, 0, this.vector, 0, size);
    }

    public TetrisVector(TetrisVector position) {
        this.size = position.size;
        this.vector = new int[size];

        System.arraycopy(position.vector, 0, this.vector, 0, size);
    }

    public int[] getVector() {
        return vector;
    }

    public int getSize() {
        return size;
    }

    public void setX(int x) {
        vector[0] = x;
    }

    public void setY(int y) {
        vector[1] = y;
    }

    public int getX() {
        return vector[0];
    }

    public int getY() {
        return vector[1];
    }

    public void setZ(int z) {
        if (size > 2) {
            vector[2] = z;
        } else {
            throw new IllegalArgumentException("Position does not have a Z coordinate");
        }
    }

    public int getZ() {
        if (size > 2) {
            return vector[2];
        } else {
            throw new IllegalArgumentException("Position does not have a Z coordinate");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TetrisVector position = (TetrisVector) o;
        return size == position.size && Arrays.equals(vector, position.vector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(vector), size);
    }

    public void add(TetrisVector other) {
        if (this.size != other.size) {
            throw new IllegalArgumentException("Positions must have the same size to be added");
        }
        for (int i = 0; i < size; i++) {
            vector[i] = vector[i] + other.vector[i];
        }
    }

    public Vector3f toVector3f() {
        if (size == 3) {
            return new Vector3f(vector[0], vector[1], vector[2]);
        } else {
            throw new IllegalArgumentException("Position does not have a Z coordinate");
        }
    }

    @Override
    public String toString() {
        return "Position{" +
                "positions=" + Arrays.toString(vector) +
                ", size=" + size +
                '}';
    }

    public void subtract(TetrisVector other) {
        if (this.size != other.size) {
            throw new IllegalArgumentException("Positions must have the same size to be added");
        }
        for (int i = 0; i < size; i++) {
            vector[i] = vector[i] - other.vector[i];
        }
    }
}
