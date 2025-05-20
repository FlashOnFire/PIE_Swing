package fr.polytech.pie.model.threeD;

import fr.polytech.pie.model.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Grid3D extends Grid {
    private final PieceColor[][][] grid;
    private final int[][] heightCache;
    private final int[][] holesCache;

    public Grid3D(Position size) {
        super(size);
        this.grid = new PieceColor[size.getZ()][size.getY()][size.getX()];
        this.heightCache = new int[size.getX()][size.getZ()];
        this.holesCache = new int[size.getX()][size.getZ()];

        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                Arrays.fill(grid[z][y], PieceColor.Empty);
            }
        }
    }

    @Override
    public PieceColor getValue(Position position) {
        if (isOutOfBounds(position)) {
            return PieceColor.Empty;
        }
        return grid[position.getZ()][position.getY()][position.getX()];
    }

    public void setValue(Position position, PieceColor value) {
        if (!isOutOfBounds(position)) {
            PieceColor oldValue = grid[position.getZ()][position.getY()][position.getX()];
            grid[position.getZ()][position.getY()][position.getX()] = value;

            if ((oldValue == PieceColor.Empty && value != PieceColor.Empty) || (oldValue != PieceColor.Empty && value == PieceColor.Empty)) {
                updateCaches(position.getX(), position.getZ());
            }
        }
    }

    @Override
    public void freezePiece(Piece piece) {
        if (!(piece instanceof Piece3D piece3D)) {
            throw new IllegalArgumentException("Expected CurrentPiece3D but got " + piece.getClass().getName());
        }

        PieceColor[][][] voxelGrid = piece3D.getPiece3d();
        Position position = piece.getPosition();

        for (int i = 0; i < piece.getWidth(); i++) {
            for (int j = 0; j < piece.getHeight(); j++) {
                for (int k = 0; k < piece3D.getDepth(); k++) {
                    if (voxelGrid[k][j][i] != PieceColor.Empty) {
                        Position pos = new Position(new int[]{i, j, k});
                        pos.add(position);
                        setValue(pos, piece.getColor());
                    }
                }
            }
        }
    }

    @Override
    public void removePiece(Piece piece) {
        if (!(piece instanceof Piece3D piece3D)) {
            throw new IllegalArgumentException("Expected CurrentPiece3D but got " + piece.getClass().getName());
        }

        for (int i = 0; i < piece.getWidth(); i++) {
            for (int j = 0; j < piece.getHeight(); j++) {
                for (int k = 0; k < piece3D.getDepth(); k++) {
                    if (piece3D.getPiece3d()[k][j][i] != PieceColor.Empty) {
                        var pos = new Position(new int[]{i, j, k});
                        pos.add(piece3D.getPosition());
                        setValue(pos, PieceColor.Empty);
                    }
                }
            }
        }
    }

    @Override
    public boolean checkCollision(Piece piece) {
        if (!(piece instanceof Piece3D piece3D)) {
            throw new IllegalArgumentException("Expected CurrentPiece3D but got " + piece.getClass().getName());
        }

        PieceColor[][][] voxelGrid = piece3D.getPiece3d();

        for (int i = 0; i < piece3D.getWidth(); i++) {
            for (int j = 0; j < piece3D.getHeight(); j++) {
                for (int k = 0; k < piece3D.getDepth(); k++) {
                    if (voxelGrid[k][j][i] != PieceColor.Empty) {
                        var pos = new Position(new int[]{i, j, k});
                        pos.add(piece3D.getPosition());

                        if (isOutOfBounds(pos)) {
                            return true;
                        }

                        if (getValue(pos) != PieceColor.Empty) {
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
        for (int y = 0; y < size.getY(); y++) {
            boolean fullPlane = true;

            // Check if the plane is full
            for (int x = 0; x < size.getX() && fullPlane; x++) {
                for (int z = 0; z < size.getZ(); z++) {
                    if (grid[z][y][x] == PieceColor.Empty) {
                        fullPlane = false;
                        break;
                    }
                }
            }

            if (fullPlane) {
                linesCleared++;

                if (!dry) {
                    // Shift all planes above down
                    for (int i = y; i < size.getY() - 1; i++) {
                        for (int x = 0; x < size.getX(); x++) {
                            for (int z = 0; z < size.getZ(); z++) {
                                grid[z][i][x] = grid[z][i + 1][x];
                            }
                        }
                    }

                    // Clear the top plane
                    for (int x = 0; x < size.getX(); x++) {
                        for (int z = 0; z < size.getZ(); z++) {
                            grid[z][size.getY() - 1][x] = PieceColor.Empty;
                        }
                    }

                    y--; // Check the same line again after shifting
                }
            }
        }

        if (!dry && linesCleared > 0) {
            recalculateAllCaches();
        }

        return linesCleared;
    }

    @Override
    public int clearFullLines() {
        return clearFullLines(false);
    }

    @Override
    public Grid copy() {
        Grid3D copy = new Grid3D(new Position(size));

        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                System.arraycopy(grid[z][y], 0, copy.grid[z][y], 0, size.getX());
            }
        }

        // Copy the height cache
        for (int x = 0; x < size.getX(); x++) {
            System.arraycopy(heightCache[x], 0, copy.heightCache[x], 0, size.getZ());
            System.arraycopy(holesCache[x], 0, copy.holesCache[x], 0, size.getZ());
        }

        return copy;
    }

    @Override
    public int getHoles() {
        int totalHoles = 0;
        for (int x = 0; x < size.getX(); x++) {
            for (int z = 0; z < size.getZ(); z++) {
                totalHoles += holesCache[x][z];
            }
        }
        return totalHoles;
    }

    private void recalculateAllCaches() {
        for (int x = 0; x < size.getX(); x++) {
            for (int z = 0; z < size.getZ(); z++) {
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

        for (int y = this.size.getY() - 1; y >= 0; y--) {
            if (grid[z][y][x] != PieceColor.Empty) {
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
    public Set<Piece> getPiecesPossibilities(Piece currentPiece) {
        if (!(currentPiece instanceof Piece3D piece3D)) {
            throw new IllegalArgumentException("Ai2D can only handle CurrentPiece2D instances");
        }

        Set<Piece> possibilities = new HashSet<>();

        // Generate rotations
        Piece3D workingPiece = piece3D.clone();
        for (var axe : RotationAxis.values()) {
            for (int i = 0; i < 4; i++) {
                workingPiece.rotate3D(axe, _ -> false, false);
                workingPiece.getPosition().setY(getHeight() - workingPiece.getHeight());
                possibilities.add(workingPiece.clone());
            }
        }

        // Generate translations
        Set<Piece> newTranslations = new HashSet<>();
        for (var piece : possibilities) {
            for (int x = 0; x < size.getX(); x++) {
                for (int z = 0; z < size.getZ(); z++) {
                    Piece3D translatedPiece = ((Piece3D) piece).clone();
                    translatedPiece.getPosition().setX(x);
                    translatedPiece.getPosition().setZ(z);
                    if (!checkCollision(translatedPiece)) {
                        newTranslations.add(translatedPiece);
                    }
                }
            }
        }
        possibilities = newTranslations;

        Set<Piece> finalPossibilities = new HashSet<>();
        for (var piece : possibilities) {
            while (!checkCollision(piece)) {
                piece.getPosition().setY(piece.getPosition().getY() - 1);
            }

            piece.getPosition().setY(piece.getPosition().getY() + 1);


            if (!checkCollision(piece)) {
                finalPossibilities.add(piece);
            }
        }

        return finalPossibilities;
    }
}
