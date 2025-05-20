package fr.polytech.pie.model.threeD;

import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.function.Predicate;

public class CurrentPiece3D extends CurrentPiece {
    private Piece[][][] piece;
    private int z;

    public CurrentPiece3D(boolean[][][] piece, int x, int y, int z) {
        super(x, y);
        this.z = z;
        this.piece = new Piece[piece.length][piece[0].length][piece[0][0].length];
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                for (int k = 0; k < piece[i][j].length; k++) {
                    if (piece[i][j][k]) {
                        this.piece[i][j][k] = color;
                    } else {
                        this.piece[i][j][k] = Piece.Empty;
                    }
                }
            }
        }
    }

    public CurrentPiece3D(Piece[][][] newGrid, int x, int y, int z, Piece color) {
        super(x, y, color);
        this.piece = newGrid;
        this.z = z;
    }

    public Piece[][][] getPiece3d() {
        return piece;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getDepth() {
        return piece.length;
    }

    @Override
    public int getWidth() {
        return piece[0][0].length;
    }

    @Override
    public int getHeight() {
        return piece[0].length;
    }

    public void translate3D(int dx, int dy, int dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }

    public void rotate3D(RotationAxis axis, Predicate<CurrentPiece> collisionChecker, boolean reverse) {
        CurrentPiece3D original = copy();

        float angle = reverse ? (float) -Math.PI / 2 : (float) Math.PI / 2;
        Matrix3f rotationMatrix = new Matrix3f();
        switch (axis) {
            case X -> rotationMatrix.rotateX(angle);
            case Y -> rotationMatrix.rotateY(angle);
            case Z -> rotationMatrix.rotateZ(angle);
        }

        applyRotation(rotationMatrix);

        if (collisionChecker.test(this)) {
            piece = original.getPiece3d();
        }
    }

    private void applyRotation(Matrix3f rotationMatrix) {
        int depth = getDepth();
        int height = getHeight();
        int width = getWidth();

        Vector3f center = calculateCenter(depth, height, width);
        
        BoundingBox bounds = calculateRotatedBounds(rotationMatrix, center, depth, height, width);
        
        piece = createRotatedPiece(rotationMatrix, center, bounds, depth, height, width);
    }
    
    private Vector3f calculateCenter(int depth, int height, int width) {
        return new Vector3f(
            (depth - 1) / 2.0f,
            (height - 1) / 2.0f,
            (width - 1) / 2.0f
        );
    }
    
    private BoundingBox calculateRotatedBounds(Matrix3f rotationMatrix, Vector3f center, 
                                              int depth, int height, int width) {
        BoundingBox bounds = new BoundingBox();
        
        for (int d = 0; d < depth; d++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    if (piece[d][h][w] != Piece.Empty) {
                        Vector3f origCoords = new Vector3f(
                            d - center.x, 
                            h - center.y, 
                            w - center.z
                        );
                        
                        Vector3f rotatedCoords = new Vector3f();
                        rotationMatrix.transform(origCoords, rotatedCoords);
                        
                        bounds.update(rotatedCoords);
                    }
                }
            }
        }
        
        return bounds;
    }
    
    private Piece[][][] createRotatedPiece(Matrix3f rotationMatrix, Vector3f center, 
                                          BoundingBox bounds, int depth, int height, int width) {
        int newDepth = (int) Math.ceil(bounds.maxX - bounds.minX + 1);
        int newHeight = (int) Math.ceil(bounds.maxY - bounds.minY + 1);
        int newWidth = (int) Math.ceil(bounds.maxZ - bounds.minZ + 1);
        
        Piece[][][] rotated = new Piece[newDepth][newHeight][newWidth];
        for (int d = 0; d < newDepth; d++) {
            for (int h = 0; h < newHeight; h++) {
                for (int w = 0; w < newWidth; w++) {
                    rotated[d][h][w] = Piece.Empty;
                }
            }
        }
        
        for (int d = 0; d < depth; d++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    if (piece[d][h][w] != Piece.Empty) {
                        Vector3f origCoords = new Vector3f(
                            d - center.x, 
                            h - center.y, 
                            w - center.z
                        );
                        
                        Vector3f rotatedCoords = new Vector3f();
                        rotationMatrix.transform(origCoords, rotatedCoords);
                        
                        int newD = Math.round(rotatedCoords.x - bounds.minX);
                        int newH = Math.round(rotatedCoords.y - bounds.minY);
                        int newW = Math.round(rotatedCoords.z - bounds.minZ);
                        
                        if (isValidIndex(newD, newH, newW, newDepth, newHeight, newWidth)) {
                            rotated[newD][newH][newW] = piece[d][h][w];
                        }
                    }
                }
            }
        }
        
        return rotated;
    }
    
    private boolean isValidIndex(int d, int h, int w, int depth, int height, int width) {
        return d >= 0 && d < depth && h >= 0 && h < height && w >= 0 && w < width;
    }
    
    private static class BoundingBox {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        float maxZ = Float.MIN_VALUE;
        
        void update(Vector3f coords) {
            minX = Math.min(minX, coords.x);
            minY = Math.min(minY, coords.y);
            minZ = Math.min(minZ, coords.z);
            maxX = Math.max(maxX, coords.x);
            maxY = Math.max(maxY, coords.y);
            maxZ = Math.max(maxZ, coords.z);
        }
    }

    public CurrentPiece3D copy() {
        Piece[][][] newGrid = new Piece[piece.length][][];
        for (int i = 0; i < piece.length; i++) {
            newGrid[i] = new Piece[piece[i].length][];
            for (int j = 0; j < piece[i].length; j++) {
                newGrid[i][j] = piece[i][j].clone();
            }
        }
        return new CurrentPiece3D(newGrid, x, y, z, color);
    }

    @Override
    public CurrentPiece3D clone() {
        CurrentPiece3D clone = (CurrentPiece3D) super.clone();

        clone.piece = new Piece[piece.length][][];
        for (int i = 0; i < piece.length; i++) {
            clone.piece[i] = new Piece[piece[i].length][];
            for (int j = 0; j < piece[i].length; j++) {
                clone.piece[i][j] = piece[i][j].clone();
            }
        }
        clone.z = this.z;
        return clone;
    }
}
