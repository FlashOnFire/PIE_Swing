package fr.polytech.pie.model.threeD;

import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.PieceColor;
import fr.polytech.pie.model.Position;
import fr.polytech.pie.model.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.function.Predicate;

public class Piece3D extends Piece {
    private PieceColor[][][] pieceColor;
    private int z;

    public Piece3D(boolean[][][] piece, Position position){
        super(position);
        this.pieceColor = new PieceColor[piece.length][piece[0].length][piece[0][0].length];
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                for (int k = 0; k < piece[i][j].length; k++) {
                    if (piece[i][j][k]) {
                        this.pieceColor[i][j][k] = color;
                    } else {
                        this.pieceColor[i][j][k] = PieceColor.Empty;
                    }
                }
            }
        }
    }

    public Piece3D(PieceColor[][][] newGrid, Position position, PieceColor color) {
        super(position, color);
        this.pieceColor = newGrid;
    }

    public PieceColor[][][] getPiece3d() {
        return pieceColor;
    }

    public int getDepth() {
        return pieceColor.length;
    }

    @Override
    public int getWidth() {
        return pieceColor[0][0].length;
    }

    @Override
    public int getHeight() {
        return pieceColor[0].length;
    }

    public void rotate3D(RotationAxis axis, Predicate<Piece> collisionChecker, boolean reverse) {
        Piece3D original = copy();

        float angle = reverse ? (float) -Math.PI / 2 : (float) Math.PI / 2;
        Matrix3f rotationMatrix = new Matrix3f();
        switch (axis) {
            case X -> rotationMatrix.rotateX(angle);
            case Y -> rotationMatrix.rotateY(angle);
            case Z -> rotationMatrix.rotateZ(angle);
        }

        applyRotation(rotationMatrix);

        if (collisionChecker.test(this)) {
            pieceColor = original.getPiece3d();
        }
    }

    private void applyRotation(Matrix3f rotationMatrix) {
        int depth = getDepth();
        int height = getHeight();
        int width = getWidth();

        Vector3f center = calculateCenter(depth, height, width);
        
        BoundingBox bounds = calculateRotatedBounds(rotationMatrix, center, depth, height, width);
        
        pieceColor = createRotatedPiece(rotationMatrix, center, bounds, depth, height, width);
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
                    if (pieceColor[d][h][w] != PieceColor.Empty) {
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
    
    private PieceColor[][][] createRotatedPiece(Matrix3f rotationMatrix, Vector3f center,
                                                BoundingBox bounds, int depth, int height, int width) {
        int newDepth = (int) Math.ceil(bounds.maxX - bounds.minX + 1);
        int newHeight = (int) Math.ceil(bounds.maxY - bounds.minY + 1);
        int newWidth = (int) Math.ceil(bounds.maxZ - bounds.minZ + 1);
        
        PieceColor[][][] rotated = new PieceColor[newDepth][newHeight][newWidth];
        for (int d = 0; d < newDepth; d++) {
            for (int h = 0; h < newHeight; h++) {
                for (int w = 0; w < newWidth; w++) {
                    rotated[d][h][w] = PieceColor.Empty;
                }
            }
        }
        
        for (int d = 0; d < depth; d++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    if (pieceColor[d][h][w] != PieceColor.Empty) {
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
                            rotated[newD][newH][newW] = pieceColor[d][h][w];
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

    public Piece3D copy() {
        PieceColor[][][] newGrid = new PieceColor[pieceColor.length][][];
        for (int i = 0; i < pieceColor.length; i++) {
            newGrid[i] = new PieceColor[pieceColor[i].length][];
            for (int j = 0; j < pieceColor[i].length; j++) {
                newGrid[i][j] = pieceColor[i][j].clone();
            }
        }
        return new Piece3D(newGrid, position, color);
    }

    @Override
    public Piece3D clone() {
        Piece3D clone = (Piece3D) super.clone();

        clone.pieceColor = new PieceColor[pieceColor.length][][];
        for (int i = 0; i < pieceColor.length; i++) {
            clone.pieceColor[i] = new PieceColor[pieceColor[i].length][];
            for (int j = 0; j < pieceColor[i].length; j++) {
                clone.pieceColor[i][j] = pieceColor[i][j].clone();
            }
        }
        clone.z = this.z;
        return clone;
    }
}
