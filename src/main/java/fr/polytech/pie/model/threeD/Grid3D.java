package fr.polytech.pie.model.threeD;

import fr.polytech.pie.model.CurrentPiece;
import fr.polytech.pie.model.Grid;
import fr.polytech.pie.model.Piece;
import fr.polytech.pie.model.RotationAxis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Grid3D extends Grid {
    private final Piece[][][] grid;
    private final int depth;
    private final int[][] heightCache;
    private final int[][] holesCache;

    public Grid3D(int width, int height, int depth) {
        super(width, height);
        this.depth = depth;
        this.grid = new Piece[depth][height][width];
        this.heightCache = new int[width][depth];
        this.holesCache = new int[width][depth];

        for (int z = 0; z < depth; z++) {
            for (int y = 0; y < height; y++) {
                Arrays.fill(grid[z][y], Piece.Empty);
            }
        }
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public Piece getValue(int x, int y) {
        for (int z = 0; z < depth; z++) {
            if (getValue(x, y, z) != Piece.Empty) {
                return getValue(x, y, z);
            }
        }
        return Piece.Empty;
    }

    public Piece getValue(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
            return Piece.Empty;
        }
        return grid[z][y][x];
    }

    public void setValue(int x, int y, int z, Piece value) {
        if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth) {
            Piece oldValue = grid[z][y][x];
            grid[z][y][x] = value;

            if ((oldValue == Piece.Empty && value != Piece.Empty) ||
                    (oldValue != Piece.Empty && value == Piece.Empty)) {
                updateCaches(x, z);
            }
        }
    }

    @Override
    public void freezePiece(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece3D)) {
            throw new IllegalArgumentException("Expected CurrentPiece3D but got " + currentPiece.getClass().getName());
        }

        Piece[][][] voxelGrid = ((CurrentPiece3D) currentPiece).getPiece3d();
        int pieceX = currentPiece.getX();
        int pieceY = currentPiece.getY();
        int pieceZ = ((CurrentPiece3D) currentPiece).getZ();

        for (int i = 0; i < currentPiece.getWidth(); i++) {
            for (int j = 0; j < currentPiece.getHeight(); j++) {
                for (int k = 0; k < ((CurrentPiece3D) currentPiece).getDepth(); k++) {
                    if (voxelGrid[k][j][i] != Piece.Empty) {
                        int x = pieceX + i;
                        int y = pieceY + j;
                        int z = pieceZ + k;
                        setValue(x, y, z, currentPiece.getColor());
                    }
                }
            }
        }
    }

    @Override
    public void removePiece(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece3D piece3D)) {
            throw new IllegalArgumentException("Expected CurrentPiece3D but got " + currentPiece.getClass().getName());
        }

        for (int i = 0; i < currentPiece.getWidth(); i++) {
            for (int j = 0; j < currentPiece.getHeight(); j++) {
                for (int k = 0; k < piece3D.getDepth(); k++) {
                    if (piece3D.getPiece3d()[k][j][i] != Piece.Empty) {
                        int x = currentPiece.getX() + i;
                        int y = currentPiece.getY() + j;
                        int z = piece3D.getZ() + k;
                        setValue(x, y, z, Piece.Empty);
                    }
                }
            }
        }
    }

    @Override
    public boolean checkCollision(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece3D piece3D)) {
            throw new IllegalArgumentException("Expected CurrentPiece3D but got " + currentPiece.getClass().getName());
        }

        Piece[][][] voxelGrid = piece3D.getPiece3d();
        int pieceX = piece3D.getX();
        int pieceY = piece3D.getY();
        int pieceZ = piece3D.getZ();

        for (int i = 0; i < piece3D.getWidth(); i++) {
            for (int j = 0; j < piece3D.getHeight(); j++) {
                for (int k = 0; k < piece3D.getDepth(); k++) {
                    if (voxelGrid[k][j][i] != Piece.Empty) {
                        int x = pieceX + i;
                        int y = pieceY + j;
                        int z = pieceZ + k;

                        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
                            return true;
                        }

                        if (getValue(x, y, z) != Piece.Empty) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int clearFullLines(boolean dry) {
        int linesCleared = 0;

        // Check for full horizontal planes (XZ planes)
        for (int y = 0; y < height; y++) {
            boolean fullPlane = true;

            // Check if the plane is full
            for (int x = 0; x < width && fullPlane; x++) {
                for (int z = 0; z < depth; z++) {
                    if (grid[z][y][x] == Piece.Empty) {
                        fullPlane = false;
                        break;
                    }
                }
            }

            if (fullPlane) {
                linesCleared++;

                if (!dry) {
                    // Shift all planes above down
                    for (int i = y; i < height - 1; i++) {
                        for (int x = 0; x < width; x++) {
                            for (int z = 0; z < depth; z++) {
                                grid[z][i][x] = grid[z][i + 1][x];
                            }
                        }
                    }

                    // Clear the top plane
                    for (int x = 0; x < width; x++) {
                        for (int z = 0; z < depth; z++) {
                            grid[z][height - 1][x] = Piece.Empty;
                        }
                    }

                    y--; // Check the same line again after shifting
                }
            }
        }

        return linesCleared;
    }

    @Override
    public int clearFullLines() {
        int linesCleared = clearFullLines(false);
        if (linesCleared > 0) {
            recalculateAllCaches();
        }
        return linesCleared;
    }

    @Override
    public Grid copy() {
        Grid3D copy = new Grid3D(width, height, depth);

        for (int z = 0; z < depth; z++) {
            for (int y = 0; y < height; y++) {
                System.arraycopy(grid[z][y], 0, copy.grid[z][y], 0, width);
            }
        }

        // Copy the height cache
        for (int x = 0; x < width; x++) {
            System.arraycopy(heightCache[x], 0, copy.heightCache[x], 0, depth);
            System.arraycopy(holesCache[x], 0, copy.holesCache[x], 0, depth);
        }

        return copy;
    }

    @Override
    public int getHoles() {
        int totalHoles = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                totalHoles += holesCache[x][z];
            }
        }
        return totalHoles;
    }

    private void recalculateAllCaches() {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                updateCaches(x, z);
            }
        }
    }

    public int getHeightOfColumn3D(int x, int z) {
        return heightCache[x][z];
    }

    private void updateCaches(int x, int z) {
        int height = 0;
        int holes = 0;
        boolean foundBlock = false;
        boolean inHole = false;

        for (int y = this.height - 1; y >= 0; y--) {
            if (grid[z][y][x] != Piece.Empty) {
                if (!foundBlock) {
                    height = y + 1;
                }
                foundBlock = true;
                inHole = false;
            } else if (foundBlock) {
                if (!inHole) {
                    holes++;
                    inHole = true;
                }
            }
        }

        heightCache[x][z] = height;
        holesCache[x][z] = holes;
    }

    @Override
    public Set<CurrentPiece> getPiecesPossibilities(CurrentPiece currentPiece) {
        if (!(currentPiece instanceof CurrentPiece3D currentPiece3D)) {
            throw new IllegalArgumentException("Ai2D can only handle CurrentPiece2D instances");
        }

        Set<CurrentPiece> possibilities = new HashSet<>();

        // Generate rotations
        CurrentPiece3D workingPiece = currentPiece3D.copy();
        for (var axe : RotationAxis.values()) {
            for (int i = 0; i < 4; i++) {
                workingPiece.rotate3D(axe, this::checkCollision, false);
                workingPiece.setY(getHeight() - workingPiece.getHeight());
                possibilities.add(workingPiece.copy());
            }
        }

        // Generate translations
        Set<CurrentPiece> newTranslations = new HashSet<>();
        for (var piece : possibilities) {
            for (int x = 0; x < getWidth(); x++) {
                for (int z = 0; z < getDepth(); z++) {
                    CurrentPiece3D translatedPiece = ((CurrentPiece3D) piece).copy();
                    translatedPiece.setX(x);
                    translatedPiece.setZ(z);
                    if (!checkCollision(translatedPiece)) {
                        newTranslations.add(translatedPiece);
                    }
                }
            }
        }
        possibilities = newTranslations;

        // Drop the pieces
        for (var piece : possibilities) {
            piece.setY(0);
            int y = 0;
            do {
                piece.setY(y++);
            } while (checkCollision(piece));
        }

        return possibilities;
    }
}
